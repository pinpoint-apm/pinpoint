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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadCountListTest {


    @Test
    public void testName() throws Exception {
        String hostName1 = "hostName1";
        String hostName2 = "hostName2";

        AgentActiveThreadCountFactory factory = new AgentActiveThreadCountFactory();
        factory.setAgentId(hostName1);
        AgentActiveThreadCount status1 = factory.createFail(TRouteResult.NOT_ACCEPTABLE.name());

        TCmdActiveThreadCountRes response = new TCmdActiveThreadCountRes();
        response.setActiveThreadCount(Arrays.asList(1, 2, 3, 4));
        factory.setAgentId(hostName2);
        AgentActiveThreadCount status2 = factory.create(response);

        AgentActiveThreadCountList list = new AgentActiveThreadCountList(5);
        list.add(status1);
        list.add(status2);

        ObjectMapper om = new ObjectMapper();
        String listAsString = om.writeValueAsString(list);

        Map map = om.readValue(listAsString, Map.class);

        Assertions.assertTrue(map.containsKey(hostName1));
        Assertions.assertTrue(map.containsKey(hostName2));

        assertDataWithSerializedJsonString((Map) map.get(hostName1), TRouteResult.NOT_ACCEPTABLE, null);
        assertDataWithSerializedJsonString((Map) map.get(hostName2), TRouteResult.OK, Arrays.asList(1, 2, 3, 4));
    }

    void assertDataWithSerializedJsonString(Map data, TRouteResult routeResult, List<Integer> status) {
        if (routeResult == TRouteResult.OK) {
            Assertions.assertEquals(data.get("code"), 0);
        } else {
            Assertions.assertEquals(data.get("code"), -1);
        }

        Assertions.assertEquals(data.get("message"), routeResult.name());

        if (status != null) {
            Assertions.assertEquals(data.get("status"), status);
        }

    }

    @Test
    public void testOrderName() {
        String hostName1 = "hostName1";
        String hostName2 = "hostName2";
        String hostName3 = "hostName3";

        AgentActiveThreadCountFactory factory = new AgentActiveThreadCountFactory();
        factory.setAgentId(hostName1);
        AgentActiveThreadCount status1 = factory.createFail("UNKNOWN ERROR");

        factory.setAgentId(hostName2);
        AgentActiveThreadCount status2 = factory.createFail("UNKNOWN ERROR");

        factory.setAgentId(hostName3);
        AgentActiveThreadCount status3 = factory.createFail("UNKNOWN ERROR");

        AgentActiveThreadCountList list = new AgentActiveThreadCountList(5);
        list.add(status2);
        list.add(status3);
        list.add(status1);

        final List<AgentActiveThreadCount> sortedList = list.getAgentActiveThreadRepository();

        Assertions.assertEquals(sortedList.get(0).getAgentId(), "hostName1");
        Assertions.assertEquals(sortedList.get(1).getAgentId(), "hostName2");
        Assertions.assertEquals(sortedList.get(2).getAgentId(), "hostName3");
    }

}
