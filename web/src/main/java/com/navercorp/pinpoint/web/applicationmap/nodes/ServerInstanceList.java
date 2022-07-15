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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.ServerInstanceListSerializer;

/**
 * @author emeroad
 * @author netspider
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@JsonSerialize(using = ServerInstanceListSerializer.class)
public class ServerInstanceList {

    private final Map<String, List<ServerInstance>> serverInstanceList = new TreeMap<>();

    public ServerInstanceList() {
    }

    public Map<String, List<ServerInstance>> getServerInstanceList() {
        // XXX list sorting problem exist
        return serverInstanceList;
    }

    public List<String> getAgentIdList() {
        Collection<List<ServerInstance>> serverList = this.serverInstanceList.values();
        return serverList.stream()
                .flatMap(List::stream)
                .map(ServerInstance::getName)
                .collect(Collectors.toList());
    }

    public Map<String, String> getAgentIdNameMap() {
        Collection<List<ServerInstance>> serverList = this.serverInstanceList.values();
        return serverList.stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(ServerInstance::getName, ServerInstance::getAgentName));
    }

    public int getInstanceCount() {
        Collection<List<ServerInstance>> serverList = this.serverInstanceList.values();
        return serverList.stream()
                .mapToInt(List::size)
                .sum();
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
        List<ServerInstance> find = serverInstanceList.computeIfAbsent(hostName, k -> new ArrayList<>());
        return find;
    }

    void addServerInstance(ServerInstance serverInstance) {
        List<ServerInstance> find = getServerInstanceList(serverInstance.getHostName());
        addServerInstance(find, serverInstance);
    }
}
