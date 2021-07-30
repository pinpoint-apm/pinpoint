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

import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class JoinDirectBufferBoTest {
    @Test
    public void joinDirectBufferBoList() throws Exception {
        List<JoinDirectBufferBo> joinDirectBufferBoList = new ArrayList<>();
        JoinDirectBufferBo joinDirectBufferBo1 = new JoinDirectBufferBo("agent1", 33, 70, "agent1", 30, "agent1"
                , 33, 70, "agent1", 30, "agent1"
                , 33, 70, "agent1", 30, "agent1"
                , 33, 70, "agent1", 30, "agent1"
                , 1496988667231L);
        JoinDirectBufferBo joinDirectBufferBo2 = new JoinDirectBufferBo("agent2", 33, 40, "agent2", 10, "agent2"
                , 33, 40, "agent2", 10, "agent2"
                , 33, 40, "agent2", 10, "agent2"
                , 33, 40, "agent2", 10, "agent2"
                , 1496988667231L);
        JoinDirectBufferBo joinDirectBufferBo3 = new JoinDirectBufferBo("agent3", 54, 60, "agent3", 7, "agent3"
                , 54, 60, "agent3", 7, "agent3"
                , 54, 60, "agent3", 7, "agent3"
                , 54, 60, "agent3", 7, "agent3"
                , 1496988667231L);
        JoinDirectBufferBo joinDirectBufferBo4 = new JoinDirectBufferBo("agent4", 11, 80, "agent4", 8, "agent4"
                , 11, 80, "agent4", 8, "agent4"
                , 11, 80, "agent4", 8, "agent4"
                , 11, 80, "agent4", 8, "agent4"
                , 1496988667231L);
        JoinDirectBufferBo joinDirectBufferBo5 = new JoinDirectBufferBo("agent5", 22, 70, "agent5", 12, "agent5"
                , 22, 70, "agent5", 12, "agent5"
                , 22, 70, "agent5", 12, "agent5"
                , 22, 70, "agent5", 12, "agent5"
                , 1496988667231L);

        joinDirectBufferBoList.add(joinDirectBufferBo1);
        joinDirectBufferBoList.add(joinDirectBufferBo2);
        joinDirectBufferBoList.add(joinDirectBufferBo3);
        joinDirectBufferBoList.add(joinDirectBufferBo4);
        joinDirectBufferBoList.add(joinDirectBufferBo5);

        JoinDirectBufferBo joinDirectBufferBo = JoinDirectBufferBo.joinDirectBufferBoList(joinDirectBufferBoList, 1496988667231L);
        assertEquals(joinDirectBufferBo.getId(), "agent1");
        assertEquals(joinDirectBufferBo.getTimestamp(), 1496988667231L);
        assertEquals(joinDirectBufferBo.getDirectCountJoinValue(), new JoinLongFieldBo(30L, 7L, "agent3", 80L, "agent4"));
        assertEquals(joinDirectBufferBo.getDirectMemoryUsedJoinValue(), new JoinLongFieldBo(30L, 7L, "agent3", 80L, "agent4"));
        assertEquals(joinDirectBufferBo.getMappedCountJoinValue(), new JoinLongFieldBo(30L, 7L, "agent3", 80L, "agent4"));
        assertEquals(joinDirectBufferBo.getMappedMemoryUsedJoinValue(), new JoinLongFieldBo(30L, 7L, "agent3", 80L, "agent4"));
   }

    @Test
    public void  joinDirectBufferBo2List() {
        List<JoinDirectBufferBo> joinDirectBufferBoList = new ArrayList<>();
        JoinDirectBufferBo joinDirectBufferBo = JoinDirectBufferBo.joinDirectBufferBoList(joinDirectBufferBoList, 1496988667231L);
        assertEquals(joinDirectBufferBo, JoinDirectBufferBo.EMPTY_JOIN_DIRECT_BUFFER_BO);
    }
}