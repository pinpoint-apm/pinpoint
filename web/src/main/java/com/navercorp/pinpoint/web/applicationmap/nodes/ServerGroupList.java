/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.nodes;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
public class ServerGroupList {
    public static final ServerGroupList EMPTY = new ServerGroupList(List.of());

    private final List<ServerGroup> serverGroupList;

    public static ServerGroupList empty() {
        return EMPTY;
    }

    private ServerGroupList(List<ServerGroup> serverGroupList) {
        this.serverGroupList = Objects.requireNonNull(serverGroupList, "serverGroupList");
    }

    public List<ServerGroup> getServerGroupList() {
        // XXX list sorting problem exist
        return serverGroupList;
    }

    public List<String> getAgentIdList() {
        List<String> list = new ArrayList<>();
        for (ServerGroup serverGroup : this.serverGroupList) {
            List<ServerInstance> instanceList = serverGroup.getInstanceList();
            for (ServerInstance serverInstance : instanceList) {
                String name = serverInstance.getName();
                list.add(name);
            }
        }
        return list;
    }

    public int getInstanceCount() {
        return this.serverGroupList.stream()
                .map(ServerGroup::getInstanceList)
                .mapToInt(List::size)
                .sum();
    }

    public boolean hasServerInstance() {
        for (ServerGroup serverGroup : this.serverGroupList) {
            if (!serverGroup.getInstanceList().isEmpty()) {
                return true;
            }
        }
        return false;
    }


    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private static final Comparator<ServerGroup> HOST_NAME_COMPARATOR = Comparator.comparing(ServerGroup::getHostName);

        private final Map<String, List<ServerInstance>> map = new HashMap<>();

        Builder() {
        }

        public void addServerInstance(ServerInstance serverInstance) {
            Objects.requireNonNull(serverInstance, "serverInstance");

            List<ServerInstance> find = getServerGroupList0(serverInstance.getHostName());
            addServerInstance(find, serverInstance);
        }

        private void addServerInstance(List<ServerInstance> nodeList, ServerInstance serverInstance) {
            if (nodeList.contains(serverInstance)) {
                return;
            }
            nodeList.add(serverInstance);
        }

        private List<ServerInstance> getServerGroupList0(String hostName) {
            return map.computeIfAbsent(hostName, k -> new ArrayList<>());
        }

        public ServerGroupList build() {
            List<ServerGroup> serverGroups = new ArrayList<>(map.size());
            for (Map.Entry<String, List<ServerInstance>> entry : map.entrySet()) {
                String hostName = entry.getKey();
                List<ServerInstance> serverInstances = entry.getValue();

                serverGroups.add(new ServerGroup(hostName, null, serverInstances));
            }
            serverGroups.sort(HOST_NAME_COMPARATOR);
            return new ServerGroupList(serverGroups);
        }

    }
}
