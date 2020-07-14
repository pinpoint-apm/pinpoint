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

package com.navercorp.pinpoint.common.server.bo.stat.join;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JoinLoadedClassBoTest {
    @Test
    public void joinLoadedClassBoListTest () {
        List<JoinLoadedClassBo> joinLoadedClassBoList = new ArrayList<JoinLoadedClassBo>();
        JoinLoadedClassBo joinLoadedClassBo1 = new JoinLoadedClassBo("agent1", 35, 10, "agent1", 67, "agent1", 35, 10, "agent1", 67, "agent1", 1496988667231L);
        JoinLoadedClassBo joinLoadedClassBo2 = new JoinLoadedClassBo("agent2", 39, 2, "agent2", 164, "agent2", 39, 2, "agent2", 164, "agent2", 1496988667231L);
        JoinLoadedClassBo joinLoadedClassBo3 = new JoinLoadedClassBo("agent3", 52, 1, "agent3", 236, "agent3",52, 1, "agent3", 236, "agent3", 1496988667231L);
        JoinLoadedClassBo joinLoadedClassBo4 = new JoinLoadedClassBo("agent4", 1, 0, "agent4", 2, "agent4",1, 0, "agent4", 2, "agent4", 1496988667231L);
        JoinLoadedClassBo joinLoadedClassBo5 = new JoinLoadedClassBo("agent5", 3, 4, "agent5", 5, "agent5", 3, 4, "agent5", 5, "agent5",1496988667231L);

        joinLoadedClassBoList.add(joinLoadedClassBo1);
        joinLoadedClassBoList.add(joinLoadedClassBo2);
        joinLoadedClassBoList.add(joinLoadedClassBo3);
        joinLoadedClassBoList.add(joinLoadedClassBo4);
        joinLoadedClassBoList.add(joinLoadedClassBo5);

        JoinLoadedClassBo joinLoadedClassBo = JoinLoadedClassBo.joinLoadedClassBoList(joinLoadedClassBoList, 1496988668231L);
        assertEquals("agent1", joinLoadedClassBo.getId());
        assertEquals(1496988668231L, joinLoadedClassBo.getTimestamp());
        assertEquals(new JoinLongFieldBo(26L, 2L, "agent4", 10L, "agent1"), joinLoadedClassBo.getLoadedClassJoinValue());
        assertEquals(new JoinLongFieldBo(26L, 2L, "agent4", 10L, "agent1"), joinLoadedClassBo.getUnloadedClassJoinValue());
    }
    @Test
    public void joinLoadedClassBoList2Test() {
        List<JoinLoadedClassBo> joinLoadedClassBoList = new ArrayList<JoinLoadedClassBo>();
        JoinLoadedClassBo joinLoadedClassBo = JoinLoadedClassBo.joinLoadedClassBoList(joinLoadedClassBoList, 1496988668231L);
        assertEquals(joinLoadedClassBo, JoinLoadedClassBo.EMPTY_JOIN_LOADED_CLASS_BO);
    }
}
