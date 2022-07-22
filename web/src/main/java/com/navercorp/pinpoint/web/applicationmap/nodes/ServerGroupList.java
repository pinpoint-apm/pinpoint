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

package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.hyperlink.LinkSources;
import com.navercorp.pinpoint.web.view.ServerGroupListSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author emeroad
 * @author netspider
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@JsonSerialize(using = ServerGroupListSerializer.class)
public class ServerGroupList {
    public static final ServerGroupList EMPTY = new ServerGroupList();

    private final List<ServerGroup> serverGroupList;

    public static ServerGroupList empty() {
        return EMPTY;
    }

    ServerGroupList() {
        this.serverGroupList = List.of();
    }

    ServerGroupList(List<ServerGroup> serverGroupList) {
        this.serverGroupList = Objects.requireNonNull(serverGroupList, "serverGroupList");
    }

    public List<ServerGroup> getServerGroupList() {
        // XXX list sorting problem exist
        return serverGroupList;
    }

    public List<String> getAgentIdList() {
        return this.serverGroupList.stream()
                .map(ServerGroup::getInstanceList)
                .flatMap(List::stream)
                .map(ServerInstance::getName)
                .collect(Collectors.toList());
    }

    public Map<String, String> getAgentIdNameMap() {
        // Stream is not recommended
        final Map<String, String> map = new LinkedHashMap<>();
        for (ServerGroup serverGroup : this.serverGroupList) {
            for (ServerInstance serverInstance : serverGroup.getInstanceList()) {
                // NPE
                map.put(serverInstance.getName(), serverInstance.getAgentName());
            }
        }
        return map;
    }

    public int getInstanceCount() {

        return this.serverGroupList.stream()
                .map(ServerGroup::getInstanceList)
                .mapToInt(List::size)
                .sum();
    }


    public static Builder newBuilder(HyperLinkFactory hyperLinkFactory) {
        return new Builder(hyperLinkFactory);
    }

    public static Builder newBuilder() {
        return new Builder(HyperLinkFactory.empty());
    }

    public static class Builder {
        private final Map<String, List<ServerInstance>> map = new HashMap<>();
        private final HyperLinkFactory hyperLinkFactory;

        Builder(HyperLinkFactory hyperLinkFactory) {
            this.hyperLinkFactory = hyperLinkFactory;
        }

        public void addServerInstance(ServerInstance serverInstance) {
            List<ServerInstance> find = getServerGroupList0(serverInstance.getHostName());
            addServerInstance(find, serverInstance);
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

        private List<ServerInstance> getServerGroupList0(String hostName) {
            return map.computeIfAbsent(hostName, k -> new ArrayList<>());
        }

        public ServerGroupList build() {
            List<Map.Entry<String, List<ServerInstance>>> sortedList = map.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());

            List<ServerGroup> serverGroups = new ArrayList<>();
            for (Map.Entry<String, List<ServerInstance>> entry : sortedList) {
                String hostName = entry.getKey();
                List<ServerInstance> serverInstances = entry.getValue();

                List<HyperLink> hyperLinks = newHyperLink(serverInstances);
                serverGroups.add(new ServerGroup(hostName, null, hyperLinks, serverInstances));
            }
            return new ServerGroupList(serverGroups);
        }

        private List<HyperLink> newHyperLink(List<ServerInstance> serverList) {
            if (hyperLinkFactory == null) {
                return List.of();
            }
            if (serverList.isEmpty()) {
                return List.of();
            }
            ServerInstance first = serverList.get(0);
            return hyperLinkFactory.build(LinkSources.from(first.getHostName(), first.getIp()));
        }
    }
}
