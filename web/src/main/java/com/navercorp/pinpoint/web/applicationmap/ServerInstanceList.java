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

import java.util.*;

import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.applicationmap.link.MatcherGroup;
import com.navercorp.pinpoint.web.applicationmap.link.ServerMatcher;
import com.navercorp.pinpoint.web.view.ServerInstanceListSerializer;

/**
 * @author emeroad
 * @author netspider
 * @author minwoo.jung
 */
@JsonSerialize(using = ServerInstanceListSerializer.class)
public class ServerInstanceList {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, List<ServerInstance>> serverInstanceList = new TreeMap<String, List<ServerInstance>>();

    private MatcherGroup matcherGroup = new MatcherGroup();

    public ServerInstanceList() {
    }

    public ServerInstanceList(MatcherGroup matcherGroup) {
        if (matcherGroup != null) {
            this.matcherGroup.addMatcherGroup(matcherGroup);
        }
    }

    public Map<String, List<ServerInstance>> getServerInstanceList() {
        // XXX list sorting problem exist
        return serverInstanceList;
    }

    public List<String> getAgentIdList() {
        final Collection<List<ServerInstance>> serverInstanceValueList = this.serverInstanceList.values();

        final List<String> agentList = new ArrayList<String>();
        for (List<ServerInstance> serverInstanceList : serverInstanceValueList) {
            for (ServerInstance serverInstance : serverInstanceList) {
                AgentInfoBo agentInfo = serverInstance.getAgentInfo();
                agentList.add(agentInfo.getAgentId());
            }
        }
        return agentList;
    }

    public int getInstanceCount() {
        int count = 0;
        for (List<ServerInstance> entry : serverInstanceList.values()) {
            count += entry.size();
        }
        return count;
    }

    private void addServerInstance(List<ServerInstance> nodeList, ServerInstance serverInstance) {
        for (ServerInstance node : nodeList) {
            boolean equalsNode = node.equals(serverInstance);
            if (equalsNode) {
                return;
            }
        }
        nodeList.add(serverInstance);
    }

    private List<ServerInstance> getServerInstanceList(String hostName) {
        List<ServerInstance> find = serverInstanceList.get(hostName);
        if (find == null) {
            find = new ArrayList<ServerInstance>();
            serverInstanceList.put(hostName, find);
        }
        return find;
    }

    void addServerInstance(ServerInstance serverInstance) {
        List<ServerInstance> find = getServerInstanceList(serverInstance.getHostName());
        addServerInstance(find, serverInstance);
    }

    public Map<String, String> getLink(String serverName) {
        ServerMatcher serverMatcher = matcherGroup.match(serverName);

        Map<String, String> linkInfo = new HashMap<String, String>();
        linkInfo.put("linkName", serverMatcher.getLinkName());
        linkInfo.put("linkURL", serverMatcher.getLink(serverName));

        return linkInfo;
    }

}
