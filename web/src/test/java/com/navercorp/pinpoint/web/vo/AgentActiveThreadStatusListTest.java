/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
public class AgentActiveThreadStatusListTest {


    @Test
    public void testName() throws Exception {
        String hostName1 = "hostName1";
        String hostName2 = "hostName2";

        AgentActiveThreadStatus status1 = new AgentActiveThreadStatus(hostName1, TRouteResult.NOT_ACCEPTABLE, null);

        TCmdActiveThreadCountRes response = new TCmdActiveThreadCountRes();
        response.setActiveThreadCount(Arrays.asList(1, 2, 3, 4));
        AgentActiveThreadStatus status2 = new AgentActiveThreadStatus(hostName2, TRouteResult.OK, response);

        AgentActiveThreadStatusList list = new AgentActiveThreadStatusList(5);
        list.add(status1);
        list.add(status2);

        ObjectMapper om = new ObjectMapper();
        String listAsString = om.writeValueAsString(list);

        Map map = om.readValue(listAsString, Map.class);

        Assert.assertTrue(map.containsKey(hostName1));
        Assert.assertTrue(map.containsKey(hostName2));

        assertDataWithSerializedJsonString((Map) map.get(hostName1), TRouteResult.NOT_ACCEPTABLE, null);
        assertDataWithSerializedJsonString((Map) map.get(hostName2), TRouteResult.OK, Arrays.asList(1, 2, 3, 4));

    }

    void assertDataWithSerializedJsonString(Map data, TRouteResult routeResult, List<Integer> status) {
        Assert.assertEquals(data.get("code"), routeResult.getValue());
        Assert.assertEquals(data.get("message"), routeResult.name());

        if (status != null) {
            Assert.assertEquals(data.get("status"), status);
        }

    }

}
