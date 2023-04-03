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

public class JoinTotalThreadCountBoTest {
    @Test
    public void joinTotalThreadCountBoList() {
        List<JoinTotalThreadCountBo> joinTotalThreadCountBoList = List.of(
                new JoinTotalThreadCountBo("agent1", 1496988667231L, 35, 10, "agent1", 67, "agent1"),
                new JoinTotalThreadCountBo("agent2", 1496988667231L, 39, 2, "agent2", 164, "agent2"),
                new JoinTotalThreadCountBo("agent3", 1496988667231L, 52, 1, "agent3", 236, "agent3"),
                new JoinTotalThreadCountBo("agent4", 1496988667231L, 1, 0, "agent4", 2, "agent4"),
                new JoinTotalThreadCountBo("agent5", 1496988667231L, 3, 4, "agent5", 5, "agent5")
        );

        JoinTotalThreadCountBo joinTotalThreadCountBo = JoinTotalThreadCountBo.joinTotalThreadCountBoList(joinTotalThreadCountBoList, 1496988668231L);
        assertEquals("agent1", joinTotalThreadCountBo.getId());
        assertEquals(1496988668231L, joinTotalThreadCountBo.getTimestamp());
        assertEquals(new JoinLongFieldBo(26L, 0L, "agent4", 236L, "agent3"), joinTotalThreadCountBo.getTotalThreadCountJoinValue());
    }

    @Test
    public void joinTotalThreadCountBoList2Test() {
        JoinTotalThreadCountBo joinTotalThreadCountBo = JoinTotalThreadCountBo.joinTotalThreadCountBoList(List.of(), 1496988668231L);
        assertEquals(joinTotalThreadCountBo, JoinTotalThreadCountBo.EMPTY_TOTAL_THREAD_COUNT_BO);
    }
}
