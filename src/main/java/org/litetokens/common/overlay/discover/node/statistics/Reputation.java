package org.litetokens.common.overlay.discover.node.statistics;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;
import org.litetokens.protos.Protocol.ReasonCode;

public class Reputation {

  public abstract class Score<T> implements Comparable<Score> {

    protected T t;

    public Score(T t) {
      this.t = t;
    }

    abstract int calculate(int baseScore);

    public boolean isContinue() {
      return true;
    }

    public int getOrder() {
      return 0;
    }

    @Override
    public int compareTo(Score score) {
      if (getOrder() > score.getOrder()) {
        return 1;
      } else if (getOrder() < score.getOrder()) {
        return -1;
      }
      return 0;
    }
  }

  public class DiscoverScore extends Score<MessageStatistics> {

    public DiscoverScore(MessageStatistics messageStatistics) {
      super(messageStatistics);
    }

    @Override
    int calculate(int baseScore) {
      int discoverReput = baseScore;
      discoverReput +=
          min(t.discoverInPong.getTotalCount(), 1) * (t.discoverOutPing.getTotalCount()
              == t.discoverInPong.getTotalCount() ? 101 : 1);
      discoverReput +=
          min(t.discoverInNeighbours.getTotalCount(), 1) * (t.discoverOutFindNode.getTotalCount()
              == t.discoverInNeighbours.getTotalCount() ? 10 : 1);
      return discoverReput;
    }

    @Override
    public boolean isContinue() {
      return t.discoverOutPing.getTotalCount() == t.discoverInPong.getTotalCount()
          && t.discoverInNeighbours.getTotalCount() <= t.discoverOutFindNode.getTotalCount();
    }
  }

  public class TcpScore extends Score<NodeStatistics> {

    public TcpScore(NodeStatistics nodeStatistics) {
      super(nodeStatistics);
    }

    @Override
    int calculate(int baseScore) {
      int reput = baseScore;
      reput += t.p2pHandShake.getTotalCount() > 0 ? 10 : 0;
      reput += min(t.tcpFlow.getTotalCount() / 10240, 20);
      reput += t.messageStatistics.p2pOutPing.getTotalCount() == t.messageStatistics.p2pInPong
          .getTotalCount() ? 10 : 0;
      return reput;
    }
  }

  public class DisConnectScore extends Score<NodeStatistics> {

    public DisConnectScore(NodeStatistics nodeStatistics) {
      super(nodeStatistics);
    }

    @Override
    int calculate(int baseScore) {
      if (t.wasDisconnected()) {
        if (t.getLitetokensLastLocalDisconnectReason() == null
            && t.getLitetokensLastRemoteDisconnectReason() == null) {
          // means connection was dropped without reporting any reason - bad
          baseScore *= 0.8;
        } else if (t.getLitetokensLastLocalDisconnectReason() != ReasonCode.REQUESTED) {
          // the disconnect was not initiated by discover mode
          if (t.getLitetokensLastRemoteDisconnectReason() == ReasonCode.TOO_MANY_PEERS
              || t.getLitetokensLastLocalDisconnectReason() == ReasonCode.TOO_MANY_PEERS
              || t.getLitetokensLastRemoteDisconnectReason() == ReasonCode.TOO_MANY_PEERS_WITH_SAME_IP
              || t.getLitetokensLastLocalDisconnectReason() == ReasonCode.TOO_MANY_PEERS_WITH_SAME_IP
              || t.getLitetokensLastRemoteDisconnectReason() == ReasonCode.DUPLICATE_PEER
              || t.getLitetokensLastLocalDisconnectReason() == ReasonCode.DUPLICATE_PEER
              || t.getLitetokensLastRemoteDisconnectReason() == ReasonCode.TIME_OUT
              || t.getLitetokensLastLocalDisconnectReason() == ReasonCode.TIME_OUT
              || t.getLitetokensLastRemoteDisconnectReason() == ReasonCode.PING_TIMEOUT
              || t.getLitetokensLastLocalDisconnectReason() == ReasonCode.PING_TIMEOUT
              || t.getLitetokensLastRemoteDisconnectReason() == ReasonCode.CONNECT_FAIL
              || t.getLitetokensLastLocalDisconnectReason() == ReasonCode.CONNECT_FAIL) {
            // The peer is popular, but we were unlucky
            baseScore *= 0.9;
          } else if (t.getLitetokensLastLocalDisconnectReason() == ReasonCode.RESET) {
            baseScore *= 0.95;
          } else if (t.getLitetokensLastRemoteDisconnectReason() != ReasonCode.REQUESTED) {
            // other disconnect reasons
            baseScore *= 0.7;
          }
        }
      }
      if (t.getDisconnectTimes() > 20) {
        return 0;
      }
      int score = baseScore - (int) Math.pow(2, t.getDisconnectTimes())
          * (t.getDisconnectTimes() > 0 ? 10 : 0);
      return score;
    }
  }

  public class OtherScore extends Score<NodeStatistics> {

    public OtherScore(NodeStatistics nodeStatistics) {
      super(nodeStatistics);
    }

    @Override
    int calculate(int baseScore) {
      baseScore += (int) t.discoverMessageLatency.getAvrg() == 0 ? 0
          : min(1000 / t.discoverMessageLatency.getAvrg(), 20);
      return baseScore;
    }
  }

  private List<Score> scoreList = new ArrayList<>();

  public Reputation(NodeStatistics nodeStatistics) {
    Score<MessageStatistics> discoverScore = new DiscoverScore(nodeStatistics.messageStatistics);
    Score<NodeStatistics> otherScore = new OtherScore(nodeStatistics);
    Score<NodeStatistics> tcpScore = new TcpScore(nodeStatistics);
    Score<NodeStatistics> disconnectScore = new DisConnectScore(nodeStatistics);

    scoreList.add(discoverScore);
    scoreList.add(tcpScore);
    scoreList.add(otherScore);
    scoreList.add(disconnectScore);
  }

  public int calculate() {
    int scoreNumber = 0;
    for (Score score : scoreList) {
      scoreNumber = score.calculate(scoreNumber);
      if (!score.isContinue()) {
        break;
      }
    }
    return scoreNumber > 0 ? scoreNumber : 0;
  }

}
