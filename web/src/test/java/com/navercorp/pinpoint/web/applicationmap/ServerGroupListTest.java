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

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author emeroad
 */
public class ServerGroupListTest {

    @Test
    public void testGetAgentIdList() {

        AgentAndStatus agentInfo1 = createAgentInfo("agentId1", "testHost");
        AgentAndStatus agentInfo2 = createAgentInfo("agentId2", "testHost");

        Set<AgentAndStatus> agentInfoSet = Set.of(agentInfo1, agentInfo2);

        ServerBuilder builder = new ServerBuilder();
        builder.addAgentInfo(agentInfoSet);
        ServerGroupList serverGroupList = builder.build();
        List<String> agentIdList = serverGroupList.getAgentIdList();

        assertThat(agentIdList).hasSize(2)
                .contains("agentId1")
                .contains("agentId2");
    }

    public static AgentAndStatus createAgentInfo(String agentId, String hostName) {
        AgentInfoBo.Builder agentInfoBuilder = new AgentInfoBo.Builder();
        agentInfoBuilder.setAgentId(AgentId.of(agentId));

        ServiceType serviceType = ServiceType.TEST_STAND_ALONE;
        agentInfoBuilder.setServiceTypeCode(serviceType.getCode());
        agentInfoBuilder.setHostName(hostName);

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);
        AgentInfoFactory factory = new AgentInfoFactory(registry);

        return new AgentAndStatus(factory.build(agentInfoBuilder.build()));

    }
}