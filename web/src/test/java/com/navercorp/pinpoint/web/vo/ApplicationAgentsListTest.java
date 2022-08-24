/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ApplicationAgentsListTest {

    private final HyperLinkFactory hyperLinkFactory = HyperLinkFactory.empty();

    @Test
    public void groupByApplicationName() {
        ApplicationAgentsList.Builder builder = ApplicationAgentsList.newBuilder(ApplicationAgentsList.GroupBy.APPLICATION_NAME, AgentInfoFilter::accept, hyperLinkFactory);
        AgentAndStatus app1Agent1 = createAgentInfo("APP_1", "app1-agent1", "Host11", true);
        AgentAndStatus app1Agent2 = createAgentInfo("APP_1", "app1-agent2", "Host12", false);
        AgentAndStatus app2Agent1 = createAgentInfo("APP_2", "app2-agent1", "Host21", false);
        AgentAndStatus app2Agent2 = createAgentInfo("APP_2", "app2-agent2", "Host22", true);
        builder.addAll(shuffleAgentInfos(app1Agent1, app1Agent2, app2Agent1, app2Agent2));

        List<ApplicationAgentList> applicationAgentLists = builder.build().getApplicationAgentLists();

        Assertions.assertEquals(2, applicationAgentLists.size());

        ApplicationAgentList app1AgentList = applicationAgentLists.get(0);
        Assertions.assertEquals("APP_1", app1AgentList.getGroupName());
        List<AgentStatusAndLink> app1AgentInfos = app1AgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(2, app1AgentInfos.size());
        Assertions.assertEquals(app1Agent1.getAgentInfo(), app1AgentInfos.get(0).getAgentInfo());
        Assertions.assertEquals(app1Agent2.getAgentInfo(), app1AgentInfos.get(1).getAgentInfo());

        ApplicationAgentList app2AgentList = applicationAgentLists.get(1);
        Assertions.assertEquals("APP_2", app2AgentList.getGroupName());
        List<AgentStatusAndLink> app2AgentInfos = app2AgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(2, app2AgentInfos.size());
        Assertions.assertEquals(app2Agent1.getAgentInfo(), app2AgentInfos.get(0).getAgentInfo());
        Assertions.assertEquals(app2Agent2.getAgentInfo(), app2AgentInfos.get(1).getAgentInfo());
    }

    @Test
    public void groupByHostNameShouldHaveContainersFirstAndGroupedSeparatelyByAgentStartTimestampDescendingOrder() {
        ApplicationAgentsList.Builder builder = ApplicationAgentsList.newBuilder(ApplicationAgentsList.GroupBy.HOST_NAME, AgentInfoFilter::accept, hyperLinkFactory);
        AgentAndStatus host1Agent1 = createAgentInfo("APP_1", "host1-agent1", "Host1", false);
        AgentAndStatus host2Agent1 = createAgentInfo("APP_1", "host2-agent1", "Host2", false);
        AgentAndStatus containerAgent1 = createAgentInfo("APP_1", "container-agent1", "Host3", true, 1);
        AgentAndStatus containerAgent2 = createAgentInfo("APP_1", "container-agent2", "Host4", true, 2);
        builder.addAll(shuffleAgentInfos(containerAgent1, host1Agent1, host2Agent1, containerAgent2));

        List<ApplicationAgentList> applicationAgentLists = builder.build().getApplicationAgentLists();

        Assertions.assertEquals(3, applicationAgentLists.size());

        ApplicationAgentList containerAgentList = applicationAgentLists.get(0);
        Assertions.assertEquals(ApplicationAgentsList.Builder.CONTAINER, containerAgentList.getGroupName());
        List<AgentStatusAndLink> containerAgents = containerAgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(2, containerAgents.size());
        Assertions.assertEquals(containerAgent2.getAgentInfo(), containerAgents.get(0).getAgentInfo());
        Assertions.assertEquals(containerAgent1.getAgentInfo(), containerAgents.get(1).getAgentInfo());

        ApplicationAgentList host1AgentList = applicationAgentLists.get(1);
        Assertions.assertEquals("Host1", host1AgentList.getGroupName());
        List<AgentStatusAndLink> host1Agents = host1AgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(1, host1Agents.size());
        Assertions.assertEquals(host1Agent1.getAgentInfo(), host1Agents.get(0).getAgentInfo());

        ApplicationAgentList host2AgentList = applicationAgentLists.get(2);
        Assertions.assertEquals("Host2", host2AgentList.getGroupName());
        List<AgentStatusAndLink> host2Agents = host2AgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(1, host2Agents.size());
        Assertions.assertEquals(host2Agent1.getAgentInfo(), host2Agents.get(0).getAgentInfo());
    }

    @Test
    public void mergeLists() {
        AgentAndStatus host1Agent1 = createAgentInfo("APP_1", "host1-agent1", "Host1", false);
        AgentAndStatus host2Agent1 = createAgentInfo("APP_1", "host2-agent1", "Host2", false);
        AgentAndStatus containerAgent1 = createAgentInfo("APP_1", "container-agent1", "Host3", true, 1);
        AgentAndStatus containerAgent2 = createAgentInfo("APP_1", "container-agent2", "Host4", true, 2);
        List<AgentAndStatus> agentInfos = shuffleAgentInfos(containerAgent1, host1Agent1, host2Agent1, containerAgent2);

        ApplicationAgentsList.Builder builder1 = ApplicationAgentsList.newBuilder(ApplicationAgentsList.GroupBy.HOST_NAME, AgentInfoFilter::accept, hyperLinkFactory);
        builder1.addAll(agentInfos.subList(0, agentInfos.size() / 2));
        ApplicationAgentsList.Builder builder2 = ApplicationAgentsList.newBuilder(ApplicationAgentsList.GroupBy.HOST_NAME, AgentInfoFilter::accept, hyperLinkFactory);
        builder2.addAll(agentInfos.subList(agentInfos.size() / 2, agentInfos.size()));

        builder1.merge(builder2.build());
        List<ApplicationAgentList> applicationAgentLists = builder1.build().getApplicationAgentLists();

        Assertions.assertEquals(3, applicationAgentLists.size());

        ApplicationAgentList containerAgentList = applicationAgentLists.get(0);
        Assertions.assertEquals(ApplicationAgentsList.Builder.CONTAINER, containerAgentList.getGroupName());
        List<AgentStatusAndLink> containerAgents = containerAgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(2, containerAgents.size());
        Assertions.assertEquals(containerAgent2.getAgentInfo(), containerAgents.get(0).getAgentInfo());
        Assertions.assertEquals(containerAgent1.getAgentInfo(), containerAgents.get(1).getAgentInfo());

        ApplicationAgentList host1AgentList = applicationAgentLists.get(1);
        Assertions.assertEquals("Host1", host1AgentList.getGroupName());
        List<AgentStatusAndLink> host1Agents = host1AgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(1, host1Agents.size());
        Assertions.assertEquals(host1Agent1.getAgentInfo(), host1Agents.get(0).getAgentInfo());

        ApplicationAgentList host2AgentList = applicationAgentLists.get(2);
        Assertions.assertEquals("Host2", host2AgentList.getGroupName());
        List<AgentStatusAndLink> host2Agents = host2AgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(1, host2Agents.size());
        Assertions.assertEquals(host2Agent1.getAgentInfo(), host2Agents.get(0).getAgentInfo());
    }

    @Test
    public void mergeListsGroupedDifferently() {
        AgentAndStatus agent1 = createAgentInfo("APP_1", "app1-agent1", "Host1", false);
        AgentAndStatus agent2 = createAgentInfo("APP_2", "app2-agent1", "Host2", false);
        AgentAndStatus agent3 = createAgentInfo("APP_2", "app2-agent2", "Host2", true);
        ApplicationAgentsList.Builder hostBuilder = ApplicationAgentsList.newBuilder(ApplicationAgentsList.GroupBy.HOST_NAME, AgentInfoFilter::accept, hyperLinkFactory);
        hostBuilder.add(agent1);
        ApplicationAgentsList.Builder appBuilder = ApplicationAgentsList.newBuilder(ApplicationAgentsList.GroupBy.APPLICATION_NAME, AgentInfoFilter::accept, hyperLinkFactory);
        appBuilder.add(agent2);
        appBuilder.add(agent3);

        hostBuilder.merge(appBuilder.build());
        List<ApplicationAgentList> applicationAgentLists = hostBuilder.build().getApplicationAgentLists();

        Assertions.assertEquals(3, applicationAgentLists.size());

        ApplicationAgentList containerAgentList = applicationAgentLists.get(0);
        Assertions.assertEquals(ApplicationAgentsList.Builder.CONTAINER, containerAgentList.getGroupName());
        List<AgentStatusAndLink> containerAgents = containerAgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(1, containerAgents.size());
        Assertions.assertEquals(agent3.getAgentInfo(), containerAgents.get(0).getAgentInfo());

        ApplicationAgentList host1AgentList = applicationAgentLists.get(1);
        Assertions.assertEquals("Host1", host1AgentList.getGroupName());
        List<AgentStatusAndLink> host1Agents = host1AgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(1, host1Agents.size());
        Assertions.assertEquals(agent1.getAgentInfo(), host1Agents.get(0).getAgentInfo());

        ApplicationAgentList host2AgentList = applicationAgentLists.get(2);
        Assertions.assertEquals("Host2", host2AgentList.getGroupName());
        List<AgentStatusAndLink> host2Agents = host2AgentList.getAgentStatusAndLinks();
        Assertions.assertEquals(1, host2Agents.size());
        Assertions.assertEquals(agent2.getAgentInfo(), host2Agents.get(0).getAgentInfo());
    }

    private static List<AgentAndStatus> shuffleAgentInfos(AgentAndStatus... agentInfos) {
        List<AgentAndStatus> agentInfoList = Arrays.asList(agentInfos);
        Collections.shuffle(agentInfoList);
        return agentInfoList;
    }

    private static AgentAndStatus createAgentInfo(String applicationName, String agentId, String hostname, boolean container) {
        return createAgentInfo(applicationName, agentId, hostname, container, System.currentTimeMillis());
    }

    private static AgentAndStatus createAgentInfo(String applicationName, String agentId, String hostname, boolean container, long startTimestamp) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName(applicationName);
        agentInfo.setAgentId(agentId);
        agentInfo.setHostName(hostname);
        agentInfo.setContainer(container);
        agentInfo.setStartTimestamp(startTimestamp);
        return new AgentAndStatus(agentInfo);
    }
}
