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

package com.navercorp.pinpoint.common.server.bo.stat.join;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class JoinCpuLoadBoTest {
    @Test
    public void joinCpuLoadBoList() throws Exception {
        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        JoinCpuLoadBo joinCpuLoadBo1 = new JoinCpuLoadBo("agent1", 44, 70, "agent1", 30, "agent1", 50, 60, "agent1", 33, "agent1", 1496988667231L);
        JoinCpuLoadBo joinCpuLoadBo2 = new JoinCpuLoadBo("agent2", 33, 40, "agent2", 10, "agent2", 20, 78, "agent2", 12, "agent2", 1496988667231L);
        JoinCpuLoadBo joinCpuLoadBo3 = new JoinCpuLoadBo("agent3", 55, 60, "agent3", 7, "agent3", 30, 39, "agent3", 30, "agent3", 1496988667231L);
        JoinCpuLoadBo joinCpuLoadBo4 = new JoinCpuLoadBo("agent4", 11, 80, "agent4", 8, "agent4", 10, 50, "agent4", 14, "agent4", 1496988667231L);
        JoinCpuLoadBo joinCpuLoadBo5 = new JoinCpuLoadBo("agent5", 22, 70, "agent5", 12, "agent5", 40, 99, "agent5", 50, "agent5", 1496988667231L);
        joinCpuLoadBoList.add(joinCpuLoadBo1);
        joinCpuLoadBoList.add(joinCpuLoadBo2);
        joinCpuLoadBoList.add(joinCpuLoadBo3);
        joinCpuLoadBoList.add(joinCpuLoadBo4);
        joinCpuLoadBoList.add(joinCpuLoadBo5);

        JoinCpuLoadBo joinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList, 1496988667231L);
        assertEquals(joinCpuLoadBo.getId(), "agent1");
        assertEquals(joinCpuLoadBo.getTimestamp(), 1496988667231L);
        assertEquals(joinCpuLoadBo.getJvmCpuLoad(), 33, 0);
        assertEquals(joinCpuLoadBo.getMinJvmCpuLoad(), 7, 0);
        assertEquals(joinCpuLoadBo.getMinJvmCpuAgentId(), "agent3");
        assertEquals(joinCpuLoadBo.getMaxJvmCpuLoad(), 80, 0);
        assertEquals(joinCpuLoadBo.getMaxJvmCpuAgentId(), "agent4");
        assertEquals(joinCpuLoadBo.getSystemCpuLoad(), 30, 0);
        assertEquals(joinCpuLoadBo.getMinSystemCpuLoad(), 12, 0);
        assertEquals(joinCpuLoadBo.getMinSysCpuAgentId(), "agent2");
        assertEquals(joinCpuLoadBo.getMaxSystemCpuLoad(), 99, 0);
        assertEquals(joinCpuLoadBo.getMaxSysCpuAgentId(), "agent5");
    }

    @Test
    public void  joinCpuLoadBo2List() {
        List<JoinCpuLoadBo> joinCpuLoadBoList = new ArrayList<JoinCpuLoadBo>();
        JoinCpuLoadBo joinCpuLoadBo = JoinCpuLoadBo.joinCpuLoadBoList(joinCpuLoadBoList, 1496988667231L);
        assertEquals(joinCpuLoadBo, JoinCpuLoadBo.EMPTY_JOIN_CPU_LOAD_BO);
    }
}