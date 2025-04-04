/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PTransaction;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class GrpcTransactionBoMapper implements GrpcStatMapper {

    public TransactionBo map(DataPoint point, final PTransaction tTransaction, long collectInterval) {
        return new TransactionBo(point,
                collectInterval,
                tTransaction.getSampledNewCount(),
                tTransaction.getSampledContinuationCount(),
                tTransaction.getUnsampledNewCount(),
                tTransaction.getUnsampledContinuationCount(),
                tTransaction.getSkippedNewCount(),
                tTransaction.getSkippedContinuationCount());
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder builder, PAgentStat agentStat) {
        // transaction
        if (agentStat.hasTransaction()) {
            DataPoint point = builder.getDataPoint();
            final PTransaction transaction = agentStat.getTransaction();
            final TransactionBo transactionBo = this.map(point, transaction, agentStat.getCollectInterval());
            builder.addPoint(transactionBo);
        }
    }
}