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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo.jung
 */
public class JoinResponseTimeBoTest {

    @Test
    public void joinResponseTimeBoListTest() {
        long time = 1496988667231L;
        List<JoinResponseTimeBo> joinResponseTimeBoList = List.of(
                new JoinResponseTimeBo("agent1", time, 3000, 2, "agent1", 6000, "agent1"),
                new JoinResponseTimeBo("agent2", time, 4000, 200, "agent2", 9000, "agent2"),
                new JoinResponseTimeBo("agent3", time, 2000, 20, "agent3", 7000, "agent3"),
                new JoinResponseTimeBo("agent4", time, 5000, 20, "agent4", 8000, "agent4"),
                new JoinResponseTimeBo("agent5", time, 1000, 10, "agent5", 6600, "agent5")
        );

        JoinResponseTimeBo joinResponseTimeBo = JoinResponseTimeBo.joinResponseTimeBoList(joinResponseTimeBoList, time);
        assertEquals("agent1", joinResponseTimeBo.getId());
        assertEquals(time, joinResponseTimeBo.getTimestamp());
        assertEquals(new JoinLongFieldBo(3000L, 2L, "agent1", 9000L, "agent2"), joinResponseTimeBo.getResponseTimeJoinValue());
    }

    @Test
    public void joinResponseTimeBoList2Test() {
        JoinResponseTimeBo joinResponseTimeBo = JoinResponseTimeBo.joinResponseTimeBoList(List.of(), 1496988667231L);
        assertEquals(joinResponseTimeBo, JoinResponseTimeBo.EMPTY_JOIN_RESPONSE_TIME_BO);
    }

}