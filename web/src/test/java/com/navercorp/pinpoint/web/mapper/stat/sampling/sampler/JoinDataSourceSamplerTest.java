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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceListBo;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class JoinDataSourceSamplerTest {

    @Test
    public void sampleDataPointsTest() {
        final String id = "test_app";
        JoinDataSourceSampler sampler = new JoinDataSourceSampler();
        long timestamp = new Date().getTime();

        AggreJoinDataSourceListBo aggreJoinDataSourceListBo = sampler.sampleDataPoints(0, timestamp, createJoinDataSourceListBoList(id, timestamp), new JoinDataSourceListBo());

        assertEquals(aggreJoinDataSourceListBo.getId(), id);
        assertEquals(aggreJoinDataSourceListBo.getTimestamp(), timestamp);
        List<AggreJoinDataSourceBo> joinDataSourceBoList = aggreJoinDataSourceListBo.getAggreJoinDataSourceBoList();
        joinDataSourceBoList.sort(new ComparatorImpl());

        assertEquals(joinDataSourceBoList.size(), 5);

        AggreJoinDataSourceBo aggreJoinDataSourceBo1 = joinDataSourceBoList.get(0);
        assertEquals(aggreJoinDataSourceBo1.getServiceTypeCode(), 1000);
        assertEquals(aggreJoinDataSourceBo1.getUrl(), "jdbc:mysql");
        assertEquals(aggreJoinDataSourceBo1.getAvgActiveConnectionSize(), 60);
        assertEquals(aggreJoinDataSourceBo1.getMinActiveConnectionSize(), 35);
        assertEquals(aggreJoinDataSourceBo1.getMinActiveConnectionAgentId(), "agent_id_1_10");
        assertEquals(aggreJoinDataSourceBo1.getMaxActiveConnectionSize(), 110);
        assertEquals(aggreJoinDataSourceBo1.getMaxActiveConnectionAgentId(), "agent_id_6_50");

        AggreJoinDataSourceBo aggreJoinDataSourceBo2 = joinDataSourceBoList.get(1);
        assertEquals(aggreJoinDataSourceBo2.getServiceTypeCode(), 2000);
        assertEquals(aggreJoinDataSourceBo2.getUrl(), "jdbc:mssql");
        assertEquals(aggreJoinDataSourceBo2.getAvgActiveConnectionSize(), 50);
        assertEquals(aggreJoinDataSourceBo2.getMinActiveConnectionSize(), 15);
        assertEquals(aggreJoinDataSourceBo2.getMinActiveConnectionAgentId(), "agent_id_2_10");
        assertEquals(aggreJoinDataSourceBo2.getMaxActiveConnectionSize(), 80);
        assertEquals(aggreJoinDataSourceBo2.getMaxActiveConnectionAgentId(), "agent_id_7_50");

        AggreJoinDataSourceBo aggreJoinDataSourceBo3 = joinDataSourceBoList.get(2);
        assertEquals(aggreJoinDataSourceBo3.getServiceTypeCode(), 3000);
        assertEquals(aggreJoinDataSourceBo3.getUrl(), "jdbc:postgre");
        assertEquals(aggreJoinDataSourceBo3.getAvgActiveConnectionSize(), 40);
        assertEquals(aggreJoinDataSourceBo3.getMinActiveConnectionSize(), 35);
        assertEquals(aggreJoinDataSourceBo3.getMinActiveConnectionAgentId(), "agent_id_3_10");
        assertEquals(aggreJoinDataSourceBo3.getMaxActiveConnectionSize(), 100);
        assertEquals(aggreJoinDataSourceBo3.getMaxActiveConnectionAgentId(), "agent_id_8_50");

        AggreJoinDataSourceBo aggreJoinDataSourceBo4 = joinDataSourceBoList.get(3);
        assertEquals(aggreJoinDataSourceBo4.getServiceTypeCode(), 4000);
        assertEquals(aggreJoinDataSourceBo4.getUrl(), "jdbc:oracle");
        assertEquals(aggreJoinDataSourceBo4.getAvgActiveConnectionSize(), 70);
        assertEquals(aggreJoinDataSourceBo4.getMinActiveConnectionSize(), 20);
        assertEquals(aggreJoinDataSourceBo4.getMinActiveConnectionAgentId(), "agent_id_4_10");
        assertEquals(aggreJoinDataSourceBo4.getMaxActiveConnectionSize(), 120);
        assertEquals(aggreJoinDataSourceBo4.getMaxActiveConnectionAgentId(), "agent_id_9_50");

        AggreJoinDataSourceBo aggreJoinDataSourceBo5 = joinDataSourceBoList.get(4);
        assertEquals(aggreJoinDataSourceBo5.getServiceTypeCode(), 5000);
        assertEquals(aggreJoinDataSourceBo5.getUrl(), "jdbc:cubrid");
        assertEquals(aggreJoinDataSourceBo5.getAvgActiveConnectionSize(), 80);
        assertEquals(aggreJoinDataSourceBo5.getMinActiveConnectionSize(), 35);
        assertEquals(aggreJoinDataSourceBo5.getMinActiveConnectionAgentId(), "agent_id_5_10");
        assertEquals(aggreJoinDataSourceBo5.getMaxActiveConnectionSize(), 130);
        assertEquals(aggreJoinDataSourceBo5.getMaxActiveConnectionAgentId(), "agent_id_10_50");
    }

    private class ComparatorImpl implements Comparator<JoinDataSourceBo> {
        @Override
        public int compare(JoinDataSourceBo bo1, JoinDataSourceBo bo2) {
            return bo1.getServiceTypeCode() < bo2.getServiceTypeCode() ? -1 : 1;
        }
    }

    private List<JoinDataSourceListBo> createJoinDataSourceListBoList(String id, long currentTime) {
        List<JoinDataSourceListBo> joinDataSourceListBoList = new ArrayList<JoinDataSourceListBo>();

        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(10), currentTime + 5000);
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(20), currentTime + 10000);
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(30), currentTime + 15000);
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(40), currentTime + 20000);
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(50), currentTime + 25000);

        joinDataSourceListBoList.add(joinDataSourceListBo1);
        joinDataSourceListBoList.add(joinDataSourceListBo2);
        joinDataSourceListBoList.add(joinDataSourceListBo3);
        joinDataSourceListBoList.add(joinDataSourceListBo4);
        joinDataSourceListBoList.add(joinDataSourceListBo5);

        return joinDataSourceListBoList;
    }

    private List<JoinDataSourceBo> createJoinDataSourceBoList(int plus) {
        List<JoinDataSourceBo> joinDataSourceBoList = new ArrayList<JoinDataSourceBo>();

        JoinDataSourceBo joinDataSourceBo1 = new JoinDataSourceBo((short) 1000, "jdbc:mysql", 30 + plus, 25 + plus, "agent_id_1_" + plus, 60 + plus, "agent_id_6_" + plus);
        JoinDataSourceBo joinDataSourceBo2 = new JoinDataSourceBo((short) 2000, "jdbc:mssql", 20 + plus, 5 + plus, "agent_id_2_" + plus, 30 + plus, "agent_id_7_" + plus);
        JoinDataSourceBo joinDataSourceBo3 = new JoinDataSourceBo((short) 3000, "jdbc:postgre", 10 + plus, 25 + plus, "agent_id_3_" + plus, 50 + plus, "agent_id_8_" + plus);
        JoinDataSourceBo joinDataSourceBo4 = new JoinDataSourceBo((short) 4000, "jdbc:oracle", 40 + plus, 10 + plus, "agent_id_4_" + plus, 70 + plus, "agent_id_9_" + plus);
        JoinDataSourceBo joinDataSourceBo5 = new JoinDataSourceBo((short) 5000, "jdbc:cubrid", 50 + plus, 25 + plus, "agent_id_5_" + plus, 80 + plus, "agent_id_10_" + plus);

        joinDataSourceBoList.add(joinDataSourceBo1);
        joinDataSourceBoList.add(joinDataSourceBo2);
        joinDataSourceBoList.add(joinDataSourceBo3);
        joinDataSourceBoList.add(joinDataSourceBo4);
        joinDataSourceBoList.add(joinDataSourceBo5);

        return joinDataSourceBoList;
    }
}