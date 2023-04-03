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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JoinLoadedClassBoTest {
    @Test
    public void joinLoadedClassBoListTest() {
        List<JoinLoadedClassBo> joinLoadedClassBoList = List.of(
                new JoinLoadedClassBo("agent1", 35, 10, "agent1", 67, "agent1", 35, 10, "agent1", 67, "agent1", 1496988667231L),
                new JoinLoadedClassBo("agent2", 39, 2, "agent2", 164, "agent2", 39, 2, "agent2", 164, "agent2", 1496988667231L),
                new JoinLoadedClassBo("agent3", 52, 1, "agent3", 236, "agent3", 52, 1, "agent3", 236, "agent3", 1496988667231L),
                new JoinLoadedClassBo("agent4", 1, 0, "agent4", 2, "agent4", 1, 0, "agent4", 2, "agent4", 1496988667231L),
                new JoinLoadedClassBo("agent5", 3, 4, "agent5", 5, "agent5", 3, 4, "agent5", 5, "agent5", 1496988667231L)
        );

        JoinLoadedClassBo joinLoadedClassBo = JoinLoadedClassBo.joinLoadedClassBoList(joinLoadedClassBoList, 1496988668231L);
        assertEquals("agent1", joinLoadedClassBo.getId());
        assertEquals(1496988668231L, joinLoadedClassBo.getTimestamp());
        assertEquals(new JoinLongFieldBo(26L, 2L, "agent4", 10L, "agent1"), joinLoadedClassBo.getLoadedClassJoinValue());
        assertEquals(new JoinLongFieldBo(26L, 2L, "agent4", 10L, "agent1"), joinLoadedClassBo.getUnloadedClassJoinValue());
    }

    @Test
    public void joinLoadedClassBoList2Test() {
        JoinLoadedClassBo joinLoadedClassBo = JoinLoadedClassBo.joinLoadedClassBoList(List.of(), 1496988668231L);
        assertEquals(joinLoadedClassBo, JoinLoadedClassBo.EMPTY_JOIN_LOADED_CLASS_BO);
    }
}
