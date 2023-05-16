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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFTransaction;
import org.apache.commons.math3.util.Precision;

/**
 * @author minwoo.jung
 */
public class JoinTransactionBoMapper implements ThriftStatMapper<JoinTransactionBo, TFAgentStat> {

    @Override
    public JoinTransactionBo map(TFAgentStat tFAgentStat) {
        if (!tFAgentStat.isSetTransaction()) {
            return JoinTransactionBo.EMPTY_JOIN_TRANSACTION_BO;
        }

        TFTransaction tFtransaction = tFAgentStat.getTransaction();
        final long totalCount = calculateTotalCount(tFtransaction, tFAgentStat.getCollectInterval());
        final String agentId = tFAgentStat.getAgentId();

        JoinTransactionBo joinTransactionBo = new JoinTransactionBo();
        joinTransactionBo.setId(agentId);
        joinTransactionBo.setCollectInterval(tFAgentStat.getCollectInterval());
        joinTransactionBo.setTimestamp(tFAgentStat.getTimestamp());
        joinTransactionBo.setTotalCountJoinValue(new JoinLongFieldBo(totalCount, totalCount, agentId, totalCount, agentId));

        return joinTransactionBo;
    }

    private long calculateTotalCount(TFTransaction tFtransaction, long collectInterval) {
        long totalCount = 0;
        totalCount += tFtransaction.getSampledNewCount();
        totalCount += tFtransaction.getSampledContinuationCount();
        totalCount += tFtransaction.getUnsampledNewCount();
        totalCount += tFtransaction.getUnsampledContinuationCount();
        totalCount += tFtransaction.getSkippedNewCount();
        totalCount += tFtransaction.getSkippedContinuationCount();

        totalCount = calculateAvgTotalCount(totalCount, collectInterval);
        return totalCount;
    }

    private static long calculateAvgTotalCount(long totalCount, long timeMs) {
        if (totalCount <= 0) {
            return totalCount;
        }

        return (long) Precision.round(totalCount / (timeMs / 1000D), 1);
    }

    @Override
    public void build(TFAgentStat tFAgentStat, JoinAgentStatBo.Builder builder) {
        JoinTransactionBo joinTransactionBo = this.map(tFAgentStat);

        if (joinTransactionBo == JoinTransactionBo.EMPTY_JOIN_TRANSACTION_BO) {
            return;
        }

        builder.addTransaction(joinTransactionBo);
    }
}
