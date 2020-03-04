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
import com.navercorp.pinpoint.web.view.ApplicationAgentsListSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@JsonSerialize(using = ApplicationAgentsListSerializer.class)
public class ApplicationAgentsList {

    public enum GroupBy {
        APPLICATION_NAME {
            @Override
            protected GroupingKey extractKey(AgentInfo agentInfo) {
                return new StringGroupingKey(agentInfo.getApplicationName());
            }

            @Override
            protected Comparator<AgentInfo> getComparator() {
                return AgentInfo.AGENT_NAME_ASC_COMPARATOR;
            }
        },
        HOST_NAME {
            @Override
            protected GroupingKey extractKey(AgentInfo agentInfo) {
                return new HostNameContainerGroupingKey(agentInfo.getHostName(), agentInfo.isContainer());
            }

            @Override
            protected Comparator<AgentInfo> getComparator() {
                return (agentInfo1, agentInfo2) -> {
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

        protected abstract GroupingKey extractKey(AgentInfo agentInfo);

        /**
         * Do not use this for sorted set and maps.
         */
        protected abstract Comparator<AgentInfo> getComparator();
    }

    /**
     * Implementations not consistent with <code>equals</code>, for internal use only.
     */
    private interface GroupingKey<T extends GroupingKey<T>> extends Comparable<T> {
        String value();
    }

    public interface Filter {

        boolean ACCEPT = true;
        boolean REJECT = false;

        boolean filter(AgentInfo agentInfo);

        Filter NONE = agentInfo -> ACCEPT;
    }

    private final GroupBy groupBy;
    private final Filter filter;
    private final SortedMap<GroupingKey, List<AgentInfo>> agentsMap = new TreeMap<>();

    public ApplicationAgentsList(GroupBy groupBy, Filter filter) {
        this.groupBy = Objects.requireNonNull(groupBy, "groupBy");
        this.filter = Objects.requireNonNull(filter, "filter");
    }

    public void add(AgentInfo agentInfo) {
        if (filter.filter(agentInfo) == Filter.REJECT) {
            return;
        }
        GroupingKey key = groupBy.extractKey(agentInfo);
        List<AgentInfo> agentInfos = agentsMap.computeIfAbsent(key, k -> new ArrayList<>());
        agentInfos.add(agentInfo);
    }

    public void addAll(Iterable<AgentInfo> agentInfos) {
        for (AgentInfo agentInfo : agentInfos) {
            add(agentInfo);
        }
    }

    public void merge(ApplicationAgentsList applicationAgentList) {
        for (List<AgentInfo> agentInfos : applicationAgentList.agentsMap.values()) {
            addAll(agentInfos);
        }
    }

    public List<ApplicationAgentList> getApplicationAgentLists() {
        if (agentsMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<ApplicationAgentList> applicationAgentLists = new ArrayList<>(agentsMap.size());
        for (Map.Entry<GroupingKey, List<AgentInfo>> e : agentsMap.entrySet()) {
            GroupingKey groupingKey = e.getKey();
            List<AgentInfo> applicationAgents = new ArrayList<>(e.getValue());
            applicationAgents.sort(groupBy.getComparator());
            applicationAgentLists.add(new ApplicationAgentList(groupingKey.value(), applicationAgents));
        }
        return applicationAgentLists;
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
