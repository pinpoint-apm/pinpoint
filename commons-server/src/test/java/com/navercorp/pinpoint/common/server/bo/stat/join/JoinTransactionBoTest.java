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
public class JoinTransactionBoTest {

    @Test
    public void joinTransactionBoLIstTest() {
        List<JoinTransactionBo> joinTransactionBoList = new ArrayList<JoinTransactionBo>();
        JoinTransactionBo joinTransactionBo1 = new JoinTransactionBo("agent1", 5000, 150, 12, "agent1", 230, "agent1", 1496988667231L);
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo("agent2", 5000, 110, 40, "agent2", 240, "agent2", 1496988667231L);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo("agent3", 5000, 120, 50, "agent3", 130, "agent3", 1496988667231L);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo("agent4", 5000, 130, 60, "agent4", 630, "agent4", 1496988667231L);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo("agent5", 5000, 140, 11, "agent5", 230, "agent5", 1496988667231L);
        joinTransactionBoList.add(joinTransactionBo1);
        joinTransactionBoList.add(joinTransactionBo2);
        joinTransactionBoList.add(joinTransactionBo3);
        joinTransactionBoList.add(joinTransactionBo4);
        joinTransactionBoList.add(joinTransactionBo5);

        JoinTransactionBo joinTransactionBo = joinTransactionBo1.joinTransactionBoLIst(joinTransactionBoList, 1496988667231L);
        assertEquals("agent1", joinTransactionBo.getId());
        assertEquals(1496988667231L, joinTransactionBo.getTimestamp());
        assertEquals(5000, joinTransactionBo.getCollectInterval());
        assertEquals(130, joinTransactionBo.getTotalCount());
        assertEquals(11, joinTransactionBo.getMinTotalCount());
        assertEquals("agent5", joinTransactionBo.getMinTotalCountAgentId());
        assertEquals(630, joinTransactionBo.getMaxTotalCount());
        assertEquals("agent4", joinTransactionBo.getMaxTotalCountAgentId());
    }

    @Test
    public void joinTransactionBoLIst2Test() {
        List<JoinTransactionBo> joinTransactionBoList = new ArrayList<JoinTransactionBo>();
        JoinTransactionBo joinTransactionBo = JoinTransactionBo.joinTransactionBoLIst(joinTransactionBoList, 1496988667231L);
        assertEquals(joinTransactionBo, JoinTransactionBo.EMPTY_TRANSACTION_BO);
    }
}