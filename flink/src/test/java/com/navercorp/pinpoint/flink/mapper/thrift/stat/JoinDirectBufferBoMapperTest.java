/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFDirectBuffer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Roy Kim
 */
public class JoinDirectBufferBoMapperTest {
    @Test
    public void mapTest() throws Exception {
        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId("testAgent");
        tFAgentStat.setTimestamp(1491274138454L);

        final TFDirectBuffer tfDirectBuffer = new TFDirectBuffer();
        tfDirectBuffer.setDirectCount(10);
        tfDirectBuffer.setDirectMemoryUsed(11);
        tfDirectBuffer.setMappedCount(12);
        tfDirectBuffer.setMappedMemoryUsed(13);
        tFAgentStat.setDirectBuffer(tfDirectBuffer);

        final JoinDirectBufferBoMapper mapper = new JoinDirectBufferBoMapper();
        final JoinDirectBufferBo joinDirectBufferBo = mapper.map(tFAgentStat);

        assertNotNull(joinDirectBufferBo);
        assertEquals(joinDirectBufferBo.getId(), "testAgent");
        assertEquals(joinDirectBufferBo.getTimestamp(), 1491274138454L);
        final JoinLongFieldBo directCountJoinValue = joinDirectBufferBo.getDirectCountJoinValue();
        assertEquals(directCountJoinValue.getAvg(), 10, 0);
        assertEquals(directCountJoinValue.getMin(), 10, 0);
        assertEquals(directCountJoinValue.getMax(), 10, 0);
        final JoinLongFieldBo directMemoryUsedJoinValue = joinDirectBufferBo.getDirectMemoryUsedJoinValue();
        assertEquals(directMemoryUsedJoinValue.getAvg(), 11, 0);
        assertEquals(directMemoryUsedJoinValue.getMin(), 11, 0);
        assertEquals(directMemoryUsedJoinValue.getMax(), 11, 0);
        final JoinLongFieldBo mappedCountJoinValue = joinDirectBufferBo.getMappedCountJoinValue();
        assertEquals(mappedCountJoinValue.getAvg(), 12, 0);
        assertEquals(mappedCountJoinValue.getMin(), 12, 0);
        assertEquals(mappedCountJoinValue.getMax(), 12, 0);
        final JoinLongFieldBo mappedMemoryUsedJoinValue = joinDirectBufferBo.getMappedMemoryUsedJoinValue();
        assertEquals(mappedMemoryUsedJoinValue.getAvg(), 13, 0);
        assertEquals(mappedMemoryUsedJoinValue.getMin(), 13, 0);
        assertEquals(mappedMemoryUsedJoinValue.getMax(), 13, 0);
    }

    @Test
    public void map2Test() {
        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId("testAgent");
        tFAgentStat.setTimestamp(1491274138454L);

        final JoinDirectBufferBoMapper mapper = new JoinDirectBufferBoMapper();
        final JoinDirectBufferBo joinDirectBufferBo = mapper.map(tFAgentStat);
        assertEquals(joinDirectBufferBo, joinDirectBufferBo.EMPTY_JOIN_DIRECT_BUFFER_BO);
    }
}