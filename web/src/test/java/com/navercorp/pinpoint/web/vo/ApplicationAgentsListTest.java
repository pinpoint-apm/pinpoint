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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ApplicationAgentsListTest {

    @Test
    public void groupByApplicationName() {
        ApplicationAgentsList applicationAgentsList = new ApplicationAgentsList(ApplicationAgentsList.GroupBy.APPLICATION_NAME, ApplicationAgentsList.Filter.NONE);
        AgentInfo app1Agent1 = createAgentInfo("APP_1", "app1-agent1", "Host11", true);
        AgentInfo app1Agent2 = createAgentInfo("APP_1", "app1-agent2", "Host12", false);
        AgentInfo app2Agent1 = createAgentInfo("APP_2", "app2-agent1", "Host21", false);
        AgentInfo app2Agent2 = createAgentInfo("APP_2", "app2-agent2", "Host22", true);
        applicationAgentsList.addAll(shuffleAgentInfos(app1Agent1, app1Agent2, app2Agent1, app2Agent2));

        List<ApplicationAgentList> applicationAgentLists = applicationAgentsList.getApplicationAgentLists();

        Assert.assertEquals(2, applicationAgentLists.size());

        ApplicationAgentList app1AgentList = applicationAgentLists.get(0);
        Assert.assertEquals("APP_1", app1AgentList.getGroupName());
        List<AgentInfo> app1AgentInfos = app1AgentList.getAgentInfos();
        Assert.assertEquals(2, app1AgentInfos.size());
        Assert.assertEquals(app1Agent1, app1AgentInfos.get(0));
        Assert.assertEquals(app1Agent2, app1AgentInfos.get(1));

        ApplicationAgentList app2AgentList = applicationAgentLists.get(1);
        Assert.assertEquals("APP_2", app2AgentList.getGroupName());
        List<AgentInfo> app2AgentInfos = app2AgentList.getAgentInfos();
        Assert.assertEquals(2, app2AgentInfos.size());
        Assert.assertEquals(app2Agent1, app2AgentInfos.get(0));
        Assert.assertEquals(app2Agent2, app2AgentInfos.get(1));
    }

    @Test
    public void groupByHostNameShouldHaveContainersFirstAndGroupedSeparatelyByAgentStartTimestampDescendingOrder() {
        ApplicationAgentsList applicationAgentsList = new ApplicationAgentsList(ApplicationAgentsList.GroupBy.HOST_NAME, ApplicationAgentsList.Filter.NONE);
        AgentInfo host1Agent1 = createAgentInfo("APP_1", "host1-agent1", "Host1", false);
        AgentInfo host2Agent1 = createAgentInfo("APP_1", "host2-agent1", "Host2", false);
        AgentInfo containerAgent1 = createAgentInfo("APP_1", "container-agent1", "Host3", true, 1);
        AgentInfo containerAgent2 = createAgentInfo("APP_1", "container-agent2", "Host4", true, 2);
        applicationAgentsList.addAll(shuffleAgentInfos(containerAgent1, host1Agent1, host2Agent1, containerAgent2));

        List<ApplicationAgentList> applicationAgentLists = applicationAgentsList.getApplicationAgentLists();

        Assert.assertEquals(3, applicationAgentLists.size());

        ApplicationAgentList containerAgentList = applicationAgentLists.get(0);
        Assert.assertEquals(ApplicationAgentsList.HostNameContainerGroupingKey.CONTAINER, containerAgentList.getGroupName());
        List<AgentInfo> containerAgents = containerAgentList.getAgentInfos();
        Assert.assertEquals(2, containerAgents.size());
        Assert.assertEquals(containerAgent2, containerAgents.get(0));
        Assert.assertEquals(containerAgent1, containerAgents.get(1));

        ApplicationAgentList host1AgentList = applicationAgentLists.get(1);
        Assert.assertEquals("Host1", host1AgentList.getGroupName());
        List<AgentInfo> host1Agents = host1AgentList.getAgentInfos();
        Assert.assertEquals(1, host1Agents.size());
        Assert.assertEquals(host1Agent1, host1Agents.get(0));

        ApplicationAgentList host2AgentList = applicationAgentLists.get(2);
        Assert.assertEquals("Host2", host2AgentList.getGroupName());
        List<AgentInfo> host2Agents = host2AgentList.getAgentInfos();
        Assert.assertEquals(1, host2Agents.size());
        Assert.assertEquals(host2Agent1, host2Agents.get(0));
    }

    @Test
    public void mergeLists() {
        AgentInfo host1Agent1 = createAgentInfo("APP_1", "host1-agent1", "Host1", false);
        AgentInfo host2Agent1 = createAgentInfo("APP_1", "host2-agent1", "Host2", false);
        AgentInfo containerAgent1 = createAgentInfo("APP_1", "container-agent1", "Host3", true, 1);
        AgentInfo containerAgent2 = createAgentInfo("APP_1", "container-agent2", "Host4", true, 2);
        List<AgentInfo> agentInfos = shuffleAgentInfos(containerAgent1, host1Agent1, host2Agent1, containerAgent2);

        ApplicationAgentsList applicationAgentsList = new ApplicationAgentsList(ApplicationAgentsList.GroupBy.HOST_NAME, ApplicationAgentsList.Filter.NONE);
        applicationAgentsList.addAll(agentInfos.subList(0, agentInfos.size() / 2));
        ApplicationAgentsList applicationAgentsListToMerge = new ApplicationAgentsList(ApplicationAgentsList.GroupBy.HOST_NAME, ApplicationAgentsList.Filter.NONE);
        applicationAgentsListToMerge.addAll(agentInfos.subList(agentInfos.size() / 2, agentInfos.size()));

        applicationAgentsList.merge(applicationAgentsListToMerge);
        List<ApplicationAgentList> applicationAgentLists = applicationAgentsList.getApplicationAgentLists();

        Assert.assertEquals(3, applicationAgentLists.size());

        ApplicationAgentList containerAgentList = applicationAgentLists.get(0);
        Assert.assertEquals(ApplicationAgentsList.HostNameContainerGroupingKey.CONTAINER, containerAgentList.getGroupName());
        List<AgentInfo> containerAgents = containerAgentList.getAgentInfos();
        Assert.assertEquals(2, containerAgents.size());
        Assert.assertEquals(containerAgent2, containerAgents.get(0));
        Assert.assertEquals(containerAgent1, containerAgents.get(1));

        ApplicationAgentList host1AgentList = applicationAgentLists.get(1);
        Assert.assertEquals("Host1", host1AgentList.getGroupName());
        List<AgentInfo> host1Agents = host1AgentList.getAgentInfos();
        Assert.assertEquals(1, host1Agents.size());
        Assert.assertEquals(host1Agent1, host1Agents.get(0));

        ApplicationAgentList host2AgentList = applicationAgentLists.get(2);
        Assert.assertEquals("Host2", host2AgentList.getGroupName());
        List<AgentInfo> host2Agents = host2AgentList.getAgentInfos();
        Assert.assertEquals(1, host2Agents.size());
        Assert.assertEquals(host2Agent1, host2Agents.get(0));
    }

    @Test
    public void mergeListsGroupedDifferently() {
        AgentInfo agent1 = createAgentInfo("APP_1", "app1-agent1", "Host1", false);
        AgentInfo agent2 = createAgentInfo("APP_2", "app2-agent1", "Host2", false);
        AgentInfo agent3 = createAgentInfo("APP_2", "app2-agent2", "Host2", true);
        ApplicationAgentsList groupedByHostnameList = new ApplicationAgentsList(ApplicationAgentsList.GroupBy.HOST_NAME, ApplicationAgentsList.Filter.NONE);
        groupedByHostnameList.add(agent1);
        ApplicationAgentsList groupedByApplicationNameList = new ApplicationAgentsList(ApplicationAgentsList.GroupBy.APPLICATION_NAME, ApplicationAgentsList.Filter.NONE);
        groupedByApplicationNameList.add(agent2);
        groupedByApplicationNameList.add(agent3);

        groupedByHostnameList.merge(groupedByApplicationNameList);
        List<ApplicationAgentList> applicationAgentLists = groupedByHostnameList.getApplicationAgentLists();

        Assert.assertEquals(3, applicationAgentLists.size());

        ApplicationAgentList containerAgentList = applicationAgentLists.get(0);
        Assert.assertEquals(ApplicationAgentsList.HostNameContainerGroupingKey.CONTAINER, containerAgentList.getGroupName());
        List<AgentInfo> containerAgents = containerAgentList.getAgentInfos();
        Assert.assertEquals(1, containerAgents.size());
        Assert.assertEquals(agent3, containerAgents.get(0));

        ApplicationAgentList host1AgentList = applicationAgentLists.get(1);
        Assert.assertEquals("Host1", host1AgentList.getGroupName());
        List<AgentInfo> host1Agents = host1AgentList.getAgentInfos();
        Assert.assertEquals(1, host1Agents.size());
        Assert.assertEquals(agent1, host1Agents.get(0));

        ApplicationAgentList host2AgentList = applicationAgentLists.get(2);
        Assert.assertEquals("Host2", host2AgentList.getGroupName());
        List<AgentInfo> host2Agents = host2AgentList.getAgentInfos();
        Assert.assertEquals(1, host2Agents.size());
        Assert.assertEquals(agent2, host2Agents.get(0));
    }

    private static List<AgentInfo> shuffleAgentInfos(AgentInfo... agentInfos) {
        List<AgentInfo> agentInfoList = Arrays.asList(agentInfos);
        Collections.shuffle(agentInfoList);
        return agentInfoList;
    }

    private static AgentInfo createAgentInfo(String applicationName, String agentId, String hostname, boolean container) {
        return createAgentInfo(applicationName, agentId, hostname, container, System.currentTimeMillis());
    }

    private static AgentInfo createAgentInfo(String applicationName, String agentId, String hostname, boolean container, long startTimestamp) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setApplicationName(applicationName);
        agentInfo.setAgentId(agentId);
        agentInfo.setHostName(hostname);
        agentInfo.setContainer(container);
        agentInfo.setStartTimestamp(startTimestamp);
        return agentInfo;
    }
}
