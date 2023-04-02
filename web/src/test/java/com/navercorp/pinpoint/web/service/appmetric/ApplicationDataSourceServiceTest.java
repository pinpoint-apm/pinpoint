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

package com.navercorp.pinpoint.web.service.appmetric;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.dao.appmetric.ApplicationMetricDao;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceListBo;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author minwoo.jung
 */
public class ApplicationDataSourceServiceTest {
    @Test
    public void classifyByDataSourceUrlTest() {
        final String id = "test_app";
        long timestamp = new Date().getTime();

        final ApplicationMetricDao<AggreJoinDataSourceListBo> applicationDataSourceDao = mock(ApplicationMetricDao.class);
        final ServiceTypeRegistryService serviceTypeRegistryService = mock(ServiceTypeRegistryService.class);
        ApplicationDataSourceService applicationDataSourceService = new ApplicationDataSourceService(applicationDataSourceDao, serviceTypeRegistryService);

        Map<JoinDataSourceListBo.DataSourceKey, List<AggreJoinDataSourceBo>> dataSourceKeyListMap = applicationDataSourceService.classifyByDataSourceUrl(createJoinDataSourceListBoList(id, timestamp));

        assertThat(dataSourceKeyListMap).hasSize(5);
    }

    private List<AggreJoinDataSourceListBo> createJoinDataSourceListBoList(String id, long currentTime) {
        return List.of(
                new AggreJoinDataSourceListBo(id, createJoinDataSourceBoList(10, currentTime + 5000), currentTime + 5000),
                new AggreJoinDataSourceListBo(id, createJoinDataSourceBoList(20, currentTime + 10000), currentTime + 10000),
                new AggreJoinDataSourceListBo(id, createJoinDataSourceBoList(30, currentTime + 15000), currentTime + 15000),
                new AggreJoinDataSourceListBo(id, createJoinDataSourceBoList(40, currentTime + 20000), currentTime + 20000),
                new AggreJoinDataSourceListBo(id, createJoinDataSourceBoList(50, currentTime + 25000), currentTime + 25000)
        );
    }

    private List<JoinDataSourceBo> createJoinDataSourceBoList(int plus, long timestamp) {
        return List.of(
                new AggreJoinDataSourceBo((short) 1000, "jdbc:mysql", 30 + plus, 25 + plus, "agent_id_1_" + plus, 60 + plus, "agent_id_6_" + plus, timestamp),
                new AggreJoinDataSourceBo((short) 2000, "jdbc:mssql", 20 + plus, 5 + plus, "agent_id_2_" + plus, 30 + plus, "agent_id_7_" + plus, timestamp),
                new AggreJoinDataSourceBo((short) 3000, "jdbc:postgre", 10 + plus, 25 + plus, "agent_id_3_" + plus, 50 + plus, "agent_id_8_" + plus, timestamp),
                new AggreJoinDataSourceBo((short) 4000, "jdbc:oracle", 40 + plus, 10 + plus, "agent_id_4_" + plus, 70 + plus, "agent_id_9_" + plus, timestamp),
                new AggreJoinDataSourceBo((short) 5000, "jdbc:cubrid", 50 + plus, 25 + plus, "agent_id_5_" + plus, 80 + plus, "agent_id_10_" + plus, timestamp)
        );
    }

}