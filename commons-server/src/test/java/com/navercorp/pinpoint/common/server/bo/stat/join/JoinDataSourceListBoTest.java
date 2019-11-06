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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class JoinDataSourceListBoTest {

    @Test
    public void joinDataSourceListBoListTest() {
        final String id = "test_app";
        final long currentTime = System.currentTimeMillis();
        final List<JoinDataSourceListBo> joinDataSourceListBoList = createJoinDataSourceListBoList(id, currentTime);
        final JoinDataSourceListBo joinDataSourceListBo = JoinDataSourceListBo.joinDataSourceListBoList(joinDataSourceListBoList, currentTime);

        assertEquals(joinDataSourceListBo.getId(), id);
        assertEquals(joinDataSourceListBo.getTimestamp(), currentTime);
        List<JoinDataSourceBo> joinDataSourceBoList = joinDataSourceListBo.getJoinDataSourceBoList();
        Collections.sort(joinDataSourceBoList, new ComparatorImpl());

        assertEquals(joinDataSourceBoList.size(), 5);

        JoinDataSourceBo joinDataSourceBo1 = joinDataSourceBoList.get(0);
        assertEquals(joinDataSourceBo1.getServiceTypeCode(), 1000);
        assertEquals(joinDataSourceBo1.getUrl(), "jdbc:mysql");
        assertEquals(joinDataSourceBo1.getAvgActiveConnectionSize(), 60);
        assertEquals(joinDataSourceBo1.getMinActiveConnectionSize(), 35);
        assertEquals(joinDataSourceBo1.getMinActiveConnectionAgentId(), "agent_id_1_10");
        assertEquals(joinDataSourceBo1.getMaxActiveConnectionSize(), 110);
        assertEquals(joinDataSourceBo1.getMaxActiveConnectionAgentId(), "agent_id_6_50");

        JoinDataSourceBo joinDataSourceBo2 = joinDataSourceBoList.get(1);
        assertEquals(joinDataSourceBo2.getServiceTypeCode(), 2000);
        assertEquals(joinDataSourceBo2.getUrl(), "jdbc:mssql");
        assertEquals(joinDataSourceBo2.getAvgActiveConnectionSize(), 50);
        assertEquals(joinDataSourceBo2.getMinActiveConnectionSize(), 15);
        assertEquals(joinDataSourceBo2.getMinActiveConnectionAgentId(), "agent_id_2_10");
        assertEquals(joinDataSourceBo2.getMaxActiveConnectionSize(), 80);
        assertEquals(joinDataSourceBo2.getMaxActiveConnectionAgentId(), "agent_id_7_50");

        JoinDataSourceBo joinDataSourceBo3 = joinDataSourceBoList.get(2);
        assertEquals(joinDataSourceBo3.getServiceTypeCode(), 3000);
        assertEquals(joinDataSourceBo3.getUrl(), "jdbc:postgre");
        assertEquals(joinDataSourceBo3.getAvgActiveConnectionSize(), 40);
        assertEquals(joinDataSourceBo3.getMinActiveConnectionSize(), 35);
        assertEquals(joinDataSourceBo3.getMinActiveConnectionAgentId(), "agent_id_3_10");
        assertEquals(joinDataSourceBo3.getMaxActiveConnectionSize(), 100);
        assertEquals(joinDataSourceBo3.getMaxActiveConnectionAgentId(), "agent_id_8_50");

        JoinDataSourceBo joinDataSourceBo4 = joinDataSourceBoList.get(3);
        assertEquals(joinDataSourceBo4.getServiceTypeCode(), 4000);
        assertEquals(joinDataSourceBo4.getUrl(), "jdbc:oracle");
        assertEquals(joinDataSourceBo4.getAvgActiveConnectionSize(), 70);
        assertEquals(joinDataSourceBo4.getMinActiveConnectionSize(), 20);
        assertEquals(joinDataSourceBo4.getMinActiveConnectionAgentId(), "agent_id_4_10");
        assertEquals(joinDataSourceBo4.getMaxActiveConnectionSize(), 120);
        assertEquals(joinDataSourceBo4.getMaxActiveConnectionAgentId(), "agent_id_9_50");


        JoinDataSourceBo joinDataSourceBo5 = joinDataSourceBoList.get(4);
        assertEquals(joinDataSourceBo5.getServiceTypeCode(), 5000);
        assertEquals(joinDataSourceBo5.getUrl(), "jdbc:cubrid");
        assertEquals(joinDataSourceBo5.getAvgActiveConnectionSize(), 80);
        assertEquals(joinDataSourceBo5.getMinActiveConnectionSize(), 35);
        assertEquals(joinDataSourceBo5.getMinActiveConnectionAgentId(), "agent_id_5_10");
        assertEquals(joinDataSourceBo5.getMaxActiveConnectionSize(), 130);
        assertEquals(joinDataSourceBo5.getMaxActiveConnectionAgentId(), "agent_id_10_50");
    }

    @Test
    public void joinDataSourceListBoList2Test() {
        final String id = "test_app";
        final long currentTime = System.currentTimeMillis();
        final List<JoinDataSourceListBo> joinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>(0);
        final JoinDataSourceListBo joinDataSourceListBo = JoinDataSourceListBo.joinDataSourceListBoList(joinDataSourceListBoList, currentTime);
        assertEquals(joinDataSourceListBo, JoinDataSourceListBo.EMPTY_JOIN_DATA_SOURCE_LIST_BO);
    }

    private class ComparatorImpl implements Comparator<JoinDataSourceBo> {
        @Override
        public int compare(JoinDataSourceBo bo1, JoinDataSourceBo bo2) {
            return bo1.getServiceTypeCode() < bo2.getServiceTypeCode() ? -1 : 1;
        }
    }

    private List<JoinDataSourceListBo> createJoinDataSourceListBoList(String id, long currentTime) {

        List<JoinDataSourceListBo> joinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>();

        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(10), currentTime);
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(20), currentTime);
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(30), currentTime);
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(40), currentTime);
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(50), currentTime);

        joinDataSourceListBoList.add(joinDataSourceListBo1);
        joinDataSourceListBoList.add(joinDataSourceListBo2);
        joinDataSourceListBoList.add(joinDataSourceListBo3);
        joinDataSourceListBoList.add(joinDataSourceListBo4);
        joinDataSourceListBoList.add(joinDataSourceListBo5);

        return joinDataSourceListBoList;
    }

    private List<JoinDataSourceBo> createJoinDataSourceBoList(int plus) {
        List<JoinDataSourceBo> joinDataSourceBoList = new ArrayList<JoinDataSourceBo>();

        JoinDataSourceBo joinDataSourceBo1 = new JoinDataSourceBo((short)1000, "jdbc:mysql", 30 + plus, 25 + plus, "agent_id_1_" + plus, 60 + plus, "agent_id_6_" + plus);
        JoinDataSourceBo joinDataSourceBo2 = new JoinDataSourceBo((short)2000, "jdbc:mssql", 20 + plus, 5 + plus, "agent_id_2_" + plus, 30 + plus, "agent_id_7_" + plus);
        JoinDataSourceBo joinDataSourceBo3 = new JoinDataSourceBo((short)3000, "jdbc:postgre", 10 + plus, 25 + plus, "agent_id_3_" + plus, 50 + plus, "agent_id_8_" + plus);
        JoinDataSourceBo joinDataSourceBo4 = new JoinDataSourceBo((short)4000, "jdbc:oracle", 40 + plus, 10 + plus, "agent_id_4_" + plus, 70 + plus, "agent_id_9_" + plus);
        JoinDataSourceBo joinDataSourceBo5 = new JoinDataSourceBo((short)5000, "jdbc:cubrid", 50 + plus, 25 + plus, "agent_id_5_" + plus, 80 + plus, "agent_id_10_" + plus);

        joinDataSourceBoList.add(joinDataSourceBo1);
        joinDataSourceBoList.add(joinDataSourceBo2);
        joinDataSourceBoList.add(joinDataSourceBo3);
        joinDataSourceBoList.add(joinDataSourceBo4);
        joinDataSourceBoList.add(joinDataSourceBo5);

        return joinDataSourceBoList;
    }


}