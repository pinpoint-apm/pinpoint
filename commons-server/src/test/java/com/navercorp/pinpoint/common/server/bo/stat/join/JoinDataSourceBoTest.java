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
public class JoinDataSourceBoTest {

    @Test
    public void joinDataSourceBoListTest() {
        List<JoinDataSourceBo> joinDataSourceBoList = new ArrayList<JoinDataSourceBo>();

        JoinDataSourceBo joinDataSourceBo1 = new JoinDataSourceBo((short)1000, "jdbc:mysql", 30, 25, "agent_id_1", 60, "agent_id_6");
        JoinDataSourceBo joinDataSourceBo2 = new JoinDataSourceBo((short)1000, "jdbc:mysql", 20, 5, "agent_id_2", 30, "agent_id_7");
        JoinDataSourceBo joinDataSourceBo3 = new JoinDataSourceBo((short)1000, "jdbc:mysql", 10, 25, "agent_id_3", 50, "agent_id_8");
        JoinDataSourceBo joinDataSourceBo4 = new JoinDataSourceBo((short)1000, "jdbc:mysql", 40, 4, "agent_id_4", 70, "agent_id_9");
        JoinDataSourceBo joinDataSourceBo5 = new JoinDataSourceBo((short)1000, "jdbc:mysql", 50, 25, "agent_id_5", 80, "agent_id_10");

        joinDataSourceBoList.add(joinDataSourceBo1);
        joinDataSourceBoList.add(joinDataSourceBo2);
        joinDataSourceBoList.add(joinDataSourceBo3);
        joinDataSourceBoList.add(joinDataSourceBo4);
        joinDataSourceBoList.add(joinDataSourceBo5);

        JoinDataSourceBo joinDataSourceBo = JoinDataSourceBo.joinDataSourceBoList(joinDataSourceBoList);

        assertEquals(joinDataSourceBo.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo.getUrl(), "jdbc:mysql");
        assertEquals(joinDataSourceBo.getAvgActiveConnectionSize(), 30);
        assertEquals(joinDataSourceBo.getMinActiveConnectionSize(), 4);
        assertEquals(joinDataSourceBo.getMinActiveConnectionAgentId(), "agent_id_4");
        assertEquals(joinDataSourceBo.getMaxActiveConnectionSize(), 80);
        assertEquals(joinDataSourceBo.getMaxActiveConnectionAgentId(), "agent_id_10");
    }

    @Test
    public void joinDataSourceBoList2Test() {
        List<JoinDataSourceBo> joinDataSourceBoList = new ArrayList<JoinDataSourceBo>();
        JoinDataSourceBo joinDataSourceBo = JoinDataSourceBo.joinDataSourceBoList(joinDataSourceBoList);

        assertEquals(joinDataSourceBo, JoinDataSourceBo.EMPTY_JOIN_DATA_SOURCE_BO);
    }

}