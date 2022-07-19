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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.web.hyperlink.HyperLink;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.hyperlink.LinkSources;
import com.navercorp.pinpoint.web.view.ApplicationAgentsListSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@JsonSerialize(using = ApplicationAgentsListSerializer.class)
public class ApplicationAgentsList {
    public enum GroupBy {
        APPLICATION_NAME {
            @Override
            protected GroupingKey extractKey(AgentAndStatus agentInfoAndStatus) {
                return new StringGroupingKey(agentInfoAndStatus.getAgentInfo().getApplicationName());
            }

            @Override
            protected Comparator<AgentAndStatus> getComparator() {
                return new Comparator<AgentAndStatus>() {
                    @Override
                    public int compare(AgentAndStatus o1, AgentAndStatus o2) {
                        return AgentInfo.AGENT_NAME_ASC_COMPARATOR.compare(o1.getAgentInfo(), o2.getAgentInfo());
                    }
                };
            }
        },
        HOST_NAME {
            @Override
            protected GroupingKey extractKey(AgentAndStatus agentInfoAndStatus) {
                AgentInfo agentInfo = agentInfoAndStatus.getAgentInfo();
                return new HostNameContainerGroupingKey(agentInfo.getHostName(), agentInfo.isContainer());
            }

            @Override
            protected Comparator<AgentAndStatus> getComparator() {
                return (agentInfoAndStatus1, agentInfoAndStatus2) -> {
                    final AgentInfo agentInfo1 = agentInfoAndStatus1.getAgentInfo();
                    final AgentInfo agentInfo2 = agentInfoAndStatus2.getAgentInfo();
                    if (agentInfo1.isContainer() && agentInfo2.isContainer()) {
                        // reverse start time order if both are containers
                        return Long.compare(agentInfo2.getStartTimestamp(), agentInfo1.getStartTimestamp());
                    }
                    if (agentInfo1.isContainer()) {
                        return -1;
                    }
                    if (agentInfo2.isContainer()) {
                        return 1;
                    }
                    // agent id order if both are not containers
                    return AgentInfo.AGENT_NAME_ASC_COMPARATOR.compare(agentInfo1, agentInfo2);
                };
            }
        };

        protected abstract GroupingKey extractKey(AgentAndStatus agentInfoAndStatus);

        /**
         * Do not use this for sorted set and maps.
         */
        protected abstract Comparator<AgentAndStatus> getComparator();
    }

    /**
     * Implementations not consistent with <code>equals</code>, for internal use only.
     */
    private interface GroupingKey<T extends GroupingKey<T>> extends Comparable<T> {
        String value();
    }


    private final GroupBy groupBy;
    private final AgentInfoFilter filter;
    private final HyperLinkFactory hyperLinkFactory;

    private final Map<GroupingKey, List<AgentAndStatus>> agentsMap = new TreeMap<>();

    public ApplicationAgentsList(GroupBy groupBy, AgentInfoFilter filter, HyperLinkFactory hyperLinkFactory) {
        this.groupBy = Objects.requireNonNull(groupBy, "groupBy");
        this.filter = Objects.requireNonNull(filter, "filter");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    public void add(AgentAndStatus agentInfoAndStatus) {
        if (filter.filter(agentInfoAndStatus) == AgentInfoFilter.REJECT) {
            return;
        }
        GroupingKey key = groupBy.extractKey(agentInfoAndStatus);
        List<AgentAndStatus> agentInfos = agentsMap.computeIfAbsent(key, k -> new ArrayList<>());
        agentInfos.add(agentInfoAndStatus);
    }

    public void addAll(Iterable<AgentAndStatus> agentInfoAndStatusList) {
        for (AgentAndStatus agent : agentInfoAndStatusList) {
            add(agent);
        }
    }

    public void merge(ApplicationAgentsList applicationAgentList) {
        for (List<AgentAndStatus> agentInfos : applicationAgentList.agentsMap.values()) {
            addAll(agentInfos);
        }
    }

    public List<ApplicationAgentList> getApplicationAgentLists() {
        if (agentsMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<ApplicationAgentList> applicationAgentLists = new ArrayList<>(agentsMap.size());
        for (Map.Entry<GroupingKey, List<AgentAndStatus>> entry : agentsMap.entrySet()) {
            final GroupingKey groupingKey = entry.getKey();
            final List<AgentAndStatus> agentInfoList = entry.getValue();

            List<AgentAndStatus> applicationAgents = new ArrayList<>(agentInfoList);
            applicationAgents.sort(groupBy.getComparator());

            List<AgentAndLink> agentInfoAndLinks = applicationAgents.stream()
                    .map(AgentAndStatus::getAgentInfo)
                    .map(this::newAgentInfoAndLink)
                    .collect(Collectors.toList());

            applicationAgentLists.add(new ApplicationAgentList(groupingKey.value(), agentInfoAndLinks));
        }
        return applicationAgentLists;
    }

    private AgentAndLink newAgentInfoAndLink(AgentInfo agentInfo) {
        List<HyperLink> hyperLinks = hyperLinkFactory.build(LinkSources.from(agentInfo));
        return new AgentAndLink(agentInfo, hyperLinks);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApplicationAgentsList{");
        sb.append("groupBy=").append(groupBy);
        sb.append(", agentsMap=").append(agentsMap);
        sb.append('}');
        return sb.toString();
    }

    @VisibleForTesting
    static class StringGroupingKey implements GroupingKey<StringGroupingKey> {

        private final String keyValue;

        private StringGroupingKey(String keyValue) {
            this.keyValue = Objects.requireNonNull(keyValue, "keyValue");
        }

        @Override
        public String value() {
            return keyValue;
        }

        @Override
        public int compareTo(StringGroupingKey o) {
            return keyValue.compareTo(o.keyValue);
        }

        @Override
        public String toString() {
            return keyValue;
        }
    }

    @VisibleForTesting
    static class HostNameContainerGroupingKey implements GroupingKey<HostNameContainerGroupingKey> {

        public static final String CONTAINER = "Container";

        private final StringGroupingKey hostNameGroupingKey;
        private final boolean isContainer;

        private HostNameContainerGroupingKey(String hostName, boolean isContainer) {
            String keyValue = Objects.requireNonNull(hostName, "hostName");
            if (isContainer) {
                keyValue = CONTAINER;
            }
            this.hostNameGroupingKey = new StringGroupingKey(keyValue);
            this.isContainer = isContainer;
        }

        @Override
        public String value() {
            if (isContainer) {
                return CONTAINER;
            }
            return hostNameGroupingKey.value();
        }

        @Override
        public int compareTo(HostNameContainerGroupingKey o) {
            if (isContainer && o.isContainer) {
                return 0;
            }
            if (isContainer) {
                return -1;
            }
            if (o.isContainer) {
                return 1;
            }
            return hostNameGroupingKey.compareTo(o.hostNameGroupingKey);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("hostName=").append(hostNameGroupingKey.value());
            sb.append(", isContainer=").append(isContainer);
            sb.append('}');
            return sb.toString();
        }
    }
}
