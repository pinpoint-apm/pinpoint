/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.flink.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.flink.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFTransaction;

/**
 * @author minwoo.jung
 */
public class JoinTransactionBoMapper implements ThriftBoMapper<JoinTransactionBo, TFAgentStat> {

    @Override
    public JoinTransactionBo map(TFAgentStat tFAgentStat) {
        if (!tFAgentStat.isSetTransaction()) {
            return JoinTransactionBo.EMPTY_JOIN_TRANSACTION_BO;
        }

        TFTransaction tFtransaction = tFAgentStat.getTransaction();
        final long totalCount = calculateTotalCount(tFtransaction);
        final String agentId = tFAgentStat.getAgentId();

        JoinTransactionBo joinTransactionBo = new JoinTransactionBo();
        joinTransactionBo.setId(agentId);
        joinTransactionBo.setCollectInterval(tFAgentStat.getCollectInterval());
        joinTransactionBo.setTimestamp(tFAgentStat.getTimestamp());
        joinTransactionBo.setTotalCount(totalCount);
        joinTransactionBo.setMaxTotalCount(totalCount);
        joinTransactionBo.setMaxTotalCountAgentId(agentId);
        joinTransactionBo.setMinTotalCount(totalCount);
        joinTransactionBo.setMinTotalCountAgentId(agentId);

        return joinTransactionBo;
    }

    private long calculateTotalCount(TFTransaction tFtransaction) {
        long totalCount = 0;
        totalCount += tFtransaction.getSampledNewCount();
        totalCount += tFtransaction.getSampledContinuationCount();
        totalCount += tFtransaction.getUnsampledNewCount();
        totalCount += tFtransaction.getUnsampledContinuationCount();
        totalCount += tFtransaction.getSkippedNewCount();
        totalCount += tFtransaction.getSkippedContinuationCount();
        return totalCount;
    }
}
