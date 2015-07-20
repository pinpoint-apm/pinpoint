/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author emeroad
 */
public class ServerInstanceListTest {

    @Test
    public void testGetAgentIdList() throws Exception {

        AgentInfoBo agentInfoBo1 = createAgentInfo("agentId1", "testHost");
        AgentInfoBo agentInfoBo2 = createAgentInfo("agentId2", "testHost");

        Set<AgentInfoBo> agentInfoBoSet = new HashSet<AgentInfoBo>();
        agentInfoBoSet.add(agentInfoBo1);
        agentInfoBoSet.add(agentInfoBo2);

        ServerBuilder builder = new ServerBuilder();
        builder.addAgentInfo(agentInfoBoSet);
        ServerInstanceList serverInstanceList = builder.build();
        List<String> agentIdList = serverInstanceList.getAgentIdList();

        Assert.assertEquals(agentIdList.size(), 2);
        Assert.assertEquals(agentIdList.get(0), "agentId1");
        Assert.assertEquals(agentIdList.get(1), "agentId2");
    }

    public static AgentInfoBo createAgentInfo(String agentId, String hostName) {
        AgentInfoBo.Builder agentInfoBuilder = new AgentInfoBo.Builder();
        agentInfoBuilder.setAgentId(agentId);

        ServiceType serviceType = ServiceType.TEST_STAND_ALONE;
        agentInfoBuilder.setServiceTypeCode(serviceType.getCode());
        // TODO FIX api
        agentInfoBuilder.setServiceType(serviceType);

        agentInfoBuilder.setHostName(hostName);

        return agentInfoBuilder.build();
    }
}