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

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.vo.AgentInfo;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;


/**
 * @author emeroad
 */
public class ServerInstanceListTest {

    @Test
    public void testGetAgentIdList() throws Exception {

        AgentInfo agentInfo1 = createAgentInfo("agentId1", "testHost");
        AgentInfo agentInfo2 = createAgentInfo("agentId2", "testHost");

        Set<AgentInfo> agentInfoSet = new HashSet<>();
        agentInfoSet.add(agentInfo1);
        agentInfoSet.add(agentInfo2);

        ServerBuilder builder = new ServerBuilder();
        builder.addAgentInfo(agentInfoSet);
        ServerInstanceList serverInstanceList = builder.build();
        List<String> agentIdList = serverInstanceList.getAgentIdList();

        Assert.assertThat(agentIdList, hasSize(2));
        Assert.assertThat(agentIdList, hasItem("agentId1"));
        Assert.assertThat(agentIdList, hasItem("agentId2"));
    }

    public static AgentInfo createAgentInfo(String agentId, String hostName) {
        AgentInfoBo.Builder agentInfoBuilder = new AgentInfoBo.Builder();
        agentInfoBuilder.setAgentId(agentId);

        ServiceType serviceType = ServiceType.TEST_STAND_ALONE;
        agentInfoBuilder.setServiceTypeCode(serviceType.getCode());
        agentInfoBuilder.setHostName(hostName);

        return new AgentInfo(agentInfoBuilder.build());
    }
}