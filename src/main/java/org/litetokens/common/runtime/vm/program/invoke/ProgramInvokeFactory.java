/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.litetokens.common.runtime.vm.program.invoke;

import org.litetokens.common.runtime.vm.DataWord;
import org.litetokens.common.runtime.vm.program.InternalTransaction;
import org.litetokens.common.runtime.vm.program.InternalTransaction.ExecutorType;
import org.litetokens.common.runtime.vm.program.Program;
import org.litetokens.common.storage.Deposit;
import org.litetokens.core.exception.ContractValidateException;
import org.litetokens.protos.Protocol.Block;
import org.litetokens.protos.Protocol.Transaction;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public interface ProgramInvokeFactory {

  ProgramInvoke createProgramInvoke(InternalTransaction.XltType xltType, ExecutorType executorType,
      Transaction tx, long tokenValue, long tokenId, Block block, Deposit deposit, long vmStartInUs, long vmShouldEndInUs,
      long energyLimit) throws ContractValidateException;

  ProgramInvoke createProgramInvoke(Program program, DataWord toAddress, DataWord callerAddress,
      DataWord inValue, DataWord tokenValue, DataWord tokenId,
      long balanceInt, byte[] dataIn, Deposit deposit, boolean staticCall, boolean byTestingSuite,
      long vmStartInUs, long vmShouldEndInUs, long energyLimit);


}
