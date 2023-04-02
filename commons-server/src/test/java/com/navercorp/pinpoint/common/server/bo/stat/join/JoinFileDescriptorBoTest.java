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
public class JoinFileDescriptorBoTest {
    @Test
    public void joinFileDescriptorBoList() {
        List<JoinFileDescriptorBo> joinFileDescriptorBoList = List.of(
                new JoinFileDescriptorBo("agent1", 33, 70, "agent1", 30, "agent1", 1496988667231L),
                new JoinFileDescriptorBo("agent2", 33, 40, "agent2", 10, "agent2", 1496988667231L),
                new JoinFileDescriptorBo("agent3", 54, 60, "agent3", 7, "agent3", 1496988667231L),
                new JoinFileDescriptorBo("agent4", 11, 80, "agent4", 8, "agent4", 1496988667231L),
                new JoinFileDescriptorBo("agent5", 22, 70, "agent5", 12, "agent5", 1496988667231L)
        );

        JoinFileDescriptorBo joinFileDescriptorBo = JoinFileDescriptorBo.joinFileDescriptorBoList(joinFileDescriptorBoList, 1496988667231L);
        assertEquals(joinFileDescriptorBo.getId(), "agent1");
        assertEquals(joinFileDescriptorBo.getTimestamp(), 1496988667231L);
        assertEquals(new JoinLongFieldBo(30L, 7L, "agent3", 80L, "agent4"), joinFileDescriptorBo.getOpenFdCountJoinValue());
    }

    @Test
    public void joinFileDescriptorBo2List() {
        JoinFileDescriptorBo joinFileDescriptorBo = JoinFileDescriptorBo.joinFileDescriptorBoList(List.of(), 1496988667231L);
        assertEquals(joinFileDescriptorBo, JoinFileDescriptorBo.EMPTY_JOIN_FILE_DESCRIPTOR_BO);
    }
}