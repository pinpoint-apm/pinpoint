/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import com.navercorp.pinpoint.thrift.dto.flink.TFLoadedClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JoinLoadedClassBoMapperTest {
    @Test
    public void mapTest() throws Exception {
        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId("testAgent");
        tFAgentStat.setTimestamp(1491274138454L);

        final TFLoadedClass tfLoadedClass = new TFLoadedClass();
        tfLoadedClass.setLoadedClassCount(50);
        tfLoadedClass.setUnloadedClassCount(50);
        tFAgentStat.setLoadedClass(tfLoadedClass);

        final JoinLoadedClassBoMapper mapper = new JoinLoadedClassBoMapper();
        final JoinLoadedClassBo joinLoadedClassBo = mapper.map(tFAgentStat);

        assertNotNull(joinLoadedClassBo);
        assertEquals("testAgent", joinLoadedClassBo.getId());
        assertEquals(1491274138454L, joinLoadedClassBo.getTimestamp());
        assertEquals(new JoinLongFieldBo(50L, 50L, "testAgent", 50L, "testAgent"), joinLoadedClassBo.getLoadedClassJoinValue());
        assertEquals(new JoinLongFieldBo(50L, 50L, "testAgent", 50L, "testAgent"), joinLoadedClassBo.getUnloadedClassJoinValue());
    }

    @Test
    public void map2Test() {
        final TFAgentStat tFAgentStat = new TFAgentStat();
        tFAgentStat.setAgentId("testAgent");
        tFAgentStat.setTimestamp(1491274138454L);

        final JoinLoadedClassBoMapper mapper = new JoinLoadedClassBoMapper();
        final JoinLoadedClassBo joinLoadedClassBo = mapper.map(tFAgentStat);
        assertEquals(joinLoadedClassBo, joinLoadedClassBo.EMPTY_JOIN_LOADED_CLASS_BO);
    }
}
