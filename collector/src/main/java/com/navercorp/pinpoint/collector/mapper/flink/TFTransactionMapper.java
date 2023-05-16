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
package com.navercorp.pinpoint.collector.mapper.flink;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class TFTransactionMapper implements FlinkStatMapper<TransactionBo, TFAgentStat> {
    public TFTransaction map(TransactionBo transactionBo) {
        TFTransaction tFTransaction = new TFTransaction();
        tFTransaction.setSampledNewCount(transactionBo.getSampledNewCount());
        tFTransaction.setSampledContinuationCount(transactionBo.getSampledContinuationCount());
        tFTransaction.setUnsampledNewCount(transactionBo.getUnsampledNewCount());
        tFTransaction.setUnsampledContinuationCount(transactionBo.getUnsampledContinuationCount());
        tFTransaction.setSkippedNewCount(transactionBo.getSkippedNewSkipCount());
        tFTransaction.setSkippedContinuationCount(transactionBo.getSkippedContinuationCount());
        return tFTransaction;
    }

    @Override
    public void map(TransactionBo transactionBo, TFAgentStat tfAgentStat) {
        tfAgentStat.setCollectInterval(transactionBo.getCollectInterval());
        tfAgentStat.setTransaction(map(transactionBo));
    }

    @Override
    public void build(TFAgentStatMapper.TFAgentStatBuilder builder) {
        AgentStatBo agentStat = builder.getAgentStat();
        List<TransactionBo> transactionList = agentStat.getTransactionBos();
        builder.build(transactionList, this);
    }
}
