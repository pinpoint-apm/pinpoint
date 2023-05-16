/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TTransaction;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class ThriftTransactionBoMapper implements ThriftStatMapper<TransactionBo, TTransaction> {

    @Override
    public TransactionBo map(TTransaction tTransaction) {
        TransactionBo transaction = new TransactionBo();
        transaction.setSampledNewCount(tTransaction.getSampledNewCount());
        transaction.setSampledContinuationCount(tTransaction.getSampledContinuationCount());
        transaction.setUnsampledNewCount(tTransaction.getUnsampledNewCount());
        transaction.setUnsampledContinuationCount(tTransaction.getUnsampledContinuationCount());
        transaction.setSkippedNewSkipCount(tTransaction.getSkippedNewCount());
        transaction.setSkippedContinuationCount(tTransaction.getSkippedContinuationCount());
        return transaction;
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder agentStatBo, TAgentStat tAgentStat) {
        // transaction
        if (tAgentStat.isSetTransaction()) {
            TransactionBo transactionBo = this.map(tAgentStat.getTransaction());
            transactionBo.setCollectInterval(tAgentStat.getCollectInterval());
            agentStatBo.addTransaction(transactionBo);
        }
    }
}