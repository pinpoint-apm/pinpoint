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

package com.navercorp.pinpoint.web.service.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.dao.ApplicationDataSourceDao;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceListBo;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author minwoo.jung
 */
public class ApplicationDataSourceServiceTest {
    @Test
    public void classifyByDataSourceUrlTest() throws Exception {
        final String id = "test_app";
        long timestamp = new Date().getTime();

        final ApplicationDataSourceDao applicationDataSourceDao = mock(ApplicationDataSourceDao.class);
        final ServiceTypeRegistryService serviceTypeRegistryService = mock(ServiceTypeRegistryService.class);
        ApplicationDataSourceService applicationDataSourceService = new ApplicationDataSourceService(applicationDataSourceDao, serviceTypeRegistryService);

        Map<JoinDataSourceListBo.DataSourceKey, List<AggreJoinDataSourceBo>> dataSourceKeyListMap = applicationDataSourceService.classifyByDataSourceUrl(createJoinDataSourceListBoList(id, timestamp));

        assertEquals(dataSourceKeyListMap.size(), 5);
    }

    private List<AggreJoinDataSourceListBo> createJoinDataSourceListBoList(String id, long currentTime) {
        List<AggreJoinDataSourceListBo> aggreJoinDataSourceListBoList = new ArrayList<AggreJoinDataSourceListBo>();

        AggreJoinDataSourceListBo aggreJoinDataSourceListBo1 = new AggreJoinDataSourceListBo(id, createJoinDataSourceBoList(10, currentTime + 5000), currentTime + 5000);
        AggreJoinDataSourceListBo aggreJoinDataSourceListBo2 = new AggreJoinDataSourceListBo(id, createJoinDataSourceBoList(20, currentTime + 10000), currentTime + 10000);
        AggreJoinDataSourceListBo aggreJoinDataSourceListBo3 = new AggreJoinDataSourceListBo(id, createJoinDataSourceBoList(30, currentTime + 15000), currentTime + 15000);
        AggreJoinDataSourceListBo aggreJoinDataSourceListBo4 = new AggreJoinDataSourceListBo(id, createJoinDataSourceBoList(40, currentTime + 20000), currentTime + 20000);
        AggreJoinDataSourceListBo aggreJoinDataSourceListBo5 = new AggreJoinDataSourceListBo(id, createJoinDataSourceBoList(50, currentTime + 25000), currentTime + 25000);

        aggreJoinDataSourceListBoList.add(aggreJoinDataSourceListBo1);
        aggreJoinDataSourceListBoList.add(aggreJoinDataSourceListBo2);
        aggreJoinDataSourceListBoList.add(aggreJoinDataSourceListBo3);
        aggreJoinDataSourceListBoList.add(aggreJoinDataSourceListBo4);
        aggreJoinDataSourceListBoList.add(aggreJoinDataSourceListBo5);

        return aggreJoinDataSourceListBoList;
    }

    private List<JoinDataSourceBo> createJoinDataSourceBoList(int plus, long timestamp) {
        List<JoinDataSourceBo> joinDataSourceBoList = new ArrayList<JoinDataSourceBo>();

        AggreJoinDataSourceBo joinDataSourceBo1 = new AggreJoinDataSourceBo((short) 1000, "jdbc:mysql", 30 + plus, 25 + plus, "agent_id_1_" + plus, 60 + plus, "agent_id_6_" + plus, timestamp);
        AggreJoinDataSourceBo joinDataSourceBo2 = new AggreJoinDataSourceBo((short) 2000, "jdbc:mssql", 20 + plus, 5 + plus, "agent_id_2_" + plus, 30 + plus, "agent_id_7_" + plus, timestamp);
        AggreJoinDataSourceBo joinDataSourceBo3 = new AggreJoinDataSourceBo((short) 3000, "jdbc:postgre", 10 + plus, 25 + plus, "agent_id_3_" + plus, 50 + plus, "agent_id_8_" + plus, timestamp);
        AggreJoinDataSourceBo joinDataSourceBo4 = new AggreJoinDataSourceBo((short) 4000, "jdbc:oracle", 40 + plus, 10 + plus, "agent_id_4_" + plus, 70 + plus, "agent_id_9_" + plus, timestamp);
        AggreJoinDataSourceBo joinDataSourceBo5 = new AggreJoinDataSourceBo((short) 5000, "jdbc:cubrid", 50 + plus, 25 + plus, "agent_id_5_" + plus, 80 + plus, "agent_id_10_" + plus, timestamp);

        joinDataSourceBoList.add(joinDataSourceBo1);
        joinDataSourceBoList.add(joinDataSourceBo2);
        joinDataSourceBoList.add(joinDataSourceBo3);
        joinDataSourceBoList.add(joinDataSourceBo4);
        joinDataSourceBoList.add(joinDataSourceBo5);

        return joinDataSourceBoList;
    }

}