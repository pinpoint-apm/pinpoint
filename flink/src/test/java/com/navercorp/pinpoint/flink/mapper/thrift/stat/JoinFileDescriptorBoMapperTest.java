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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFFileDescriptor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Roy Kim
 */
public class JoinFileDescriptorBoMapperTest {
    @Test
    public void mapTest() throws Exception {
        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId("testAgent");
        tFAgentStat.setTimestamp(1491274138454L);

        final TFFileDescriptor tfFileDescriptor = new TFFileDescriptor();
        tfFileDescriptor.setOpenFileDescriptorCount(10);
        tFAgentStat.setFileDescriptor(tfFileDescriptor);

        final JoinFileDescriptorBoMapper mapper = new JoinFileDescriptorBoMapper();
        final JoinFileDescriptorBo joinFileDescriptorBo = mapper.map(tFAgentStat);

        assertNotNull(joinFileDescriptorBo);
        assertEquals(joinFileDescriptorBo.getId(), "testAgent");
        assertEquals(joinFileDescriptorBo.getTimestamp(), 1491274138454L);
        assertEquals(joinFileDescriptorBo.getAvgOpenFDCount(), 10, 0);
        assertEquals(joinFileDescriptorBo.getMinOpenFDCount(), 10, 0);
        assertEquals(joinFileDescriptorBo.getMaxOpenFDCount(), 10, 0);
    }

    @Test
    public void map2Test() {
        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId("testAgent");
        tFAgentStat.setTimestamp(1491274138454L);

        final JoinFileDescriptorBoMapper mapper = new JoinFileDescriptorBoMapper();
        final JoinFileDescriptorBo joinFileDescriptorBo = mapper.map(tFAgentStat);
        assertEquals(joinFileDescriptorBo, joinFileDescriptorBo.EMPTY_JOIN_FILE_DESCRIPTOR_BO);
    }
}