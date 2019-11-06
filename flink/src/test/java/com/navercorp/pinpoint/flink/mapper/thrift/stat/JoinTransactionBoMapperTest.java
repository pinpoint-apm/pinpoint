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
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFTransaction;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class JoinTransactionBoMapperTest {

    @Test
    public void mapTest() throws Exception {
        final TFAgentStat tFAgentStat = new TFAgentStat();
        final String id = "testAgent";
        tFAgentStat.setAgentId(id);
        tFAgentStat.setTimestamp(1491274138454L);
        tFAgentStat.setCollectInterval(5000);

        final TFTransaction tFTransaction = new TFTransaction();
        tFTransaction.setSampledNewCount(10);
        tFTransaction.setSampledContinuationCount(20);
        tFTransaction.setUnsampledNewCount(40);
        tFTransaction.setUnsampledContinuationCount(50);
        tFTransaction.setSkippedNewCount(60);
        tFTransaction.setSkippedContinuationCount(70);
        tFAgentStat.setTransaction(tFTransaction);

        final JoinTransactionBoMapper joinTransactionBoMapper = new JoinTransactionBoMapper();
        final JoinTransactionBo joinTransactionBo = joinTransactionBoMapper.map(tFAgentStat);

        assertNotNull(joinTransactionBo);
        assertEquals(joinTransactionBo.getId(), id);
        assertEquals(joinTransactionBo.getTimestamp(), 1491274138454L);
        assertEquals(joinTransactionBo.getCollectInterval(), 5000);
        assertEquals(joinTransactionBo.getTotalCount(), 250);
        assertEquals(joinTransactionBo.getMaxTotalCount(), 250);
        assertEquals(joinTransactionBo.getMaxTotalCountAgentId(), id);
        assertEquals(joinTransactionBo.getMinTotalCount(), 250);
        assertEquals(joinTransactionBo.getMinTotalCountAgentId(), id);
    }

    @Test
    public void map2Test() {
        final TFAgentStat tFAgentStat = new TFAgentStat();
        final String id = "testAgent";
        tFAgentStat.setAgentId(id);
        tFAgentStat.setTimestamp(1491274138454L);
        tFAgentStat.setCollectInterval(5000);

        final JoinTransactionBoMapper joinTransactionBoMapper = new JoinTransactionBoMapper();
        final JoinTransactionBo joinTransactionBo = joinTransactionBoMapper.map(tFAgentStat);

        assertEquals(joinTransactionBo, JoinTransactionBo.EMPTY_JOIN_TRANSACTION_BO);
    }
}