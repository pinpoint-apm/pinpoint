/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.tree;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
public class ApplicationAgentHostList {

    private final int startApplicationIndex;
    private final int endApplicationIndex;
    private final int totalApplications;

    private final List<ApplicationInfo> applications;

    public ApplicationAgentHostList(int startApplicationIndex, int endApplicationIndex, int totalApplications,
                                    List<ApplicationInfo> applications) {
        this.startApplicationIndex = startApplicationIndex;
        this.endApplicationIndex = endApplicationIndex;
        this.totalApplications = totalApplications;
        this.applications = Objects.requireNonNull(applications, "applications");
    }

    @JsonProperty("startIndex")
    public int getStartApplicationIndex() {
        return startApplicationIndex;
    }

    @JsonProperty("endIndex")
    public int getEndApplicationIndex() {
        return endApplicationIndex;
    }

    @JsonProperty("totalApplications")
    public int getTotalApplications() {
        return totalApplications;
    }

    public List<ApplicationInfo> getApplications() {
        return applications;
    }

    @Override
    public String toString() {
        return "ApplicationAgentHostList{" +
                "startApplicationIndex=" + startApplicationIndex +
                ", endApplicationIndex=" + endApplicationIndex +
                ", totalApplications=" + totalApplications +
                ", applications=" + applications +
                '}';
    }

    public static Builder newBuilder(int startApplicationIndex, int endApplicationIndex, int totalApplications) {
        return new Builder(startApplicationIndex, endApplicationIndex, totalApplications);
    }

    public static class Builder {
        private final int startApplicationIndex;
        private final int endApplicationIndex;
        private final int totalApplications;

        private final Map<String, List<AgentHost>> map = new HashMap<>();

        public Builder(int startApplicationIndex, int endApplicationIndex, int totalApplications) {
            this.startApplicationIndex = startApplicationIndex;
            this.endApplicationIndex = endApplicationIndex;
            this.totalApplications = totalApplications;
        }


        public void addAgentInfo(String applicationName, List<AgentInfo> agentInfoList) {
            if (applicationName == null) {
                return;
            }

            List<AgentHost> value = map.computeIfAbsent(applicationName, k -> new ArrayList<>());

            if (agentInfoList == null) {
                return;
            }

            for (AgentInfo agentInfo : agentInfoList) {
                if (agentInfo != null) {
                    value.add(newAgentHost(agentInfo));
                }
            }
        }

        private AgentHost newAgentHost(AgentInfo agentInfo) {
            String agentId = StringUtils.defaultString(agentInfo.getAgentId(), "");
            String hostName = StringUtils.defaultString(agentInfo.getHostName(), "");
            String ip = StringUtils.defaultString(agentInfo.getIp(), "");
            String serviceType = agentInfo.getServiceType().getDesc();
            return new AgentHost(agentId, hostName, ip, serviceType);
        }

        public ApplicationAgentHostList build() {
            List<ApplicationInfo> applicationInfos = buildApplicationInfo(this.map);
            ApplicationAgentHostList agents = new ApplicationAgentHostList(startApplicationIndex, endApplicationIndex, totalApplications,
                    applicationInfos);
            return agents;
        }

        private List<ApplicationInfo> buildApplicationInfo(Map<String, List<AgentHost>> map) {
            List<ApplicationInfo> applications = map.entrySet().stream()
                    .map(Builder::newApplication)
                    .sorted(Comparator.comparing(ApplicationInfo::getApplicationName))
                    .collect(Collectors.toList());
            return applications;
        }


        private static ApplicationInfo newApplication(Map.Entry<String, List<AgentHost>> entry) {
            String applicationName = entry.getKey();

            List<AgentHost> agentHosts = entry.getValue();
            agentHosts.sort(Comparator.comparing(AgentHost::getAgentId));

            return new ApplicationInfo(applicationName, agentHosts);
        }
    }

    public static class ApplicationInfo {
        private final String applicationName;
        private final List<AgentHost> agents;

        public ApplicationInfo(String applicationName, List<AgentHost> agents) {
            this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
            this.agents = Objects.requireNonNull(agents, "agents");
        }

        public String getApplicationName() {
            return applicationName;
        }

        public List<AgentHost> getAgents() {
            return agents;
        }
    }

    public static class AgentHost {
        private final String agentId;
        private final String hostName;
        private final String ip;
        private final String serviceType;

        public AgentHost(String agentId, String hostName, String ip, String serviceType) {
            this.agentId = Objects.requireNonNull(agentId, "agentId");
            this.hostName = Objects.requireNonNull(hostName, "hostName");
            this.ip = Objects.requireNonNull(ip, "ip");
            this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        }

        public String getAgentId() {
            return agentId;
        }

        public String getHostName() {
            return hostName;
        }

        public String getIp() {
            return ip;
        }

        public String getServiceType() {
            return serviceType;
        }
    }

}