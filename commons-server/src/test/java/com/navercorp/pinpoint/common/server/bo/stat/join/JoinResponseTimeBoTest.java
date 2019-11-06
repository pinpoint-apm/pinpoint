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
public class JoinResponseTimeBoTest {

    @Test
    public void joinResponseTimeBoListTest() {
        long time = 1496988667231L;
        List<JoinResponseTimeBo> joinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();
        JoinResponseTimeBo joinResponseTimeBo1 = new JoinResponseTimeBo("agent1", time, 3000, 2, "agent1", 6000, "agent1");
        JoinResponseTimeBo joinResponseTimeBo2 = new JoinResponseTimeBo("agent2", time, 4000, 200, "agent2", 9000, "agent2");
        JoinResponseTimeBo joinResponseTimeBo3 = new JoinResponseTimeBo("agent3", time, 2000, 20, "agent3", 7000, "agent3");
        JoinResponseTimeBo joinResponseTimeBo4 = new JoinResponseTimeBo("agent4", time, 5000, 20, "agent4", 8000, "agent4");
        JoinResponseTimeBo joinResponseTimeBo5 = new JoinResponseTimeBo("agent5", time, 1000, 10, "agent5", 6600, "agent5");
        joinResponseTimeBoList.add(joinResponseTimeBo1);
        joinResponseTimeBoList.add(joinResponseTimeBo2);
        joinResponseTimeBoList.add(joinResponseTimeBo3);
        joinResponseTimeBoList.add(joinResponseTimeBo4);
        joinResponseTimeBoList.add(joinResponseTimeBo5);

        JoinResponseTimeBo joinResponseTimeBo = JoinResponseTimeBo.joinResponseTimeBoList(joinResponseTimeBoList, time);
        assertEquals("agent1", joinResponseTimeBo.getId());
        assertEquals(time, joinResponseTimeBo.getTimestamp());
        assertEquals(3000, joinResponseTimeBo.getAvg());
        assertEquals(2, joinResponseTimeBo.getMinAvg());
        assertEquals("agent1", joinResponseTimeBo.getMinAvgAgentId());
        assertEquals(9000, joinResponseTimeBo.getMaxAvg());
        assertEquals("agent2", joinResponseTimeBo.getMaxAvgAgentId());
    }

    @Test
    public void joinResponseTimeBoList2Test() {
        List<JoinResponseTimeBo> joinResponseTimeBoList = new ArrayList<JoinResponseTimeBo>();
        JoinResponseTimeBo joinResponseTimeBo = JoinResponseTimeBo.joinResponseTimeBoList(joinResponseTimeBoList, 1496988667231L);
        assertEquals(joinResponseTimeBo, JoinResponseTimeBo.EMPTY_JOIN_RESPONSE_TIME_BO);
    }

}