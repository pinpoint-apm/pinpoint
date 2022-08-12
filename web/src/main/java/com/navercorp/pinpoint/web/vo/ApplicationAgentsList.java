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
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@JsonSerialize(using = ApplicationAgentsListSerializer.class)
public class ApplicationAgentsList {
    public enum GroupBy {
        APPLICATION_NAME {
            @Override
            protected GroupingKey<StringGroupingKey> extractKey(AgentAndStatus agentInfoAndStatus) {
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
            protected GroupingKey<HostNameContainerGroupingKey> extractKey(AgentAndStatus agentInfoAndStatus) {
                AgentInfo agentInfo = agentInfoAndStatus.getAgentInfo();
                return new HostNameContainerGroupingKey(agentInfo.getHostName(), agentInfo.isContainer());
            }

            @Override
            protected Comparator<AgentAndStatus> getComparator() {
                return (agentInfoAndStatus1, agentInfoAndStatus2) -> {
                    final AgentInfo agentInfo1 = agentInfoAndStatus1.getAgentInfo();
                    final AgentInfo agentInfo2 = agentInfoAndStatus2.getAgentInfo();

                    int containerEquals = CONTAINER_FIRST_COMPARE.compare(agentInfo1.isContainer(), agentInfo2.isContainer());
                    if (containerEquals != 0) {
                        return containerEquals;
                    }
                    // reverse start time order if both are containers
                    int startTime = Long.compare(agentInfo2.getStartTimestamp(), agentInfo1.getStartTimestamp());
                    if (startTime != 0) {
                        return startTime;
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
    private interface GroupingKey<T> extends Comparable<T> {
        String value();
    }



    private final List<ApplicationAgentList> list;

    public ApplicationAgentsList(List<ApplicationAgentList> list) {
        this.list = Objects.requireNonNull(list, "list");
    }

    public List<ApplicationAgentList> getApplicationAgentLists() {
       return list;
    }

    public static Builder newBuilder(GroupBy groupBy, AgentInfoFilter filter, HyperLinkFactory hyperLinkFactory) {
        return new Builder(groupBy, filter, hyperLinkFactory);
    }


    @Override
    public String toString() {
        return "ApplicationAgentsList{" +
                "list=" + list +
                '}';
    }


    public static class Builder {

        private final GroupBy groupBy;
        private final AgentInfoFilter filter;
        private final HyperLinkFactory hyperLinkFactory;

        private final List<AgentAndStatus> list = new ArrayList<>();


        Builder(GroupBy groupBy, AgentInfoFilter filter, HyperLinkFactory hyperLinkFactory) {
            this.groupBy = Objects.requireNonNull(groupBy, "groupBy");
            this.filter = Objects.requireNonNull(filter, "filter");
            this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        }

        public void add(AgentAndStatus agentInfoAndStatus) {
//            if (filter.filter(agentInfoAndStatus) == AgentInfoFilter.REJECT) {
//                return;
//            }
//            GroupingKey<?> key = groupBy.extractKey(agentInfoAndStatus);
//            List<AgentAndStatus> agentInfos = list.computeIfAbsent(key, k -> new ArrayList<>());
//            agentInfos.add(agentInfoAndStatus);
            this.list.add(agentInfoAndStatus);
        }

        public void addAll(Collection<AgentAndStatus> agentInfoAndStatusList) {
            Objects.requireNonNull(agentInfoAndStatusList, "agentInfoAndStatusList");
            for (AgentAndStatus agent : agentInfoAndStatusList) {
                Objects.requireNonNull(agent, "agent");
                add(agent);
            }
        }

        public void merge(ApplicationAgentsList applicationAgentList) {
            for (ApplicationAgentList agentList : applicationAgentList.getApplicationAgentLists()) {
                for (AgentStatusAndLink agent : agentList.getAgentStatusAndLinks()) {
                    add(new AgentAndStatus(agent.getAgentInfo(), agent.getStatus()));
                }
            }
        }

        public ApplicationAgentsList build() {
            if (list.isEmpty()) {
                return new ApplicationAgentsList(List.of());
            }
            if (groupBy == GroupBy.APPLICATION_NAME) {
                return new ApplicationAgentsList(groupByApplicationName(list));
            }

            if (groupBy == GroupBy.HOST_NAME) {
                return new ApplicationAgentsList(groupByHostName(list));
            }
            throw new UnsupportedOperationException("dd");
        }

        private Stream<AgentAndStatus> filteredStream(List<AgentAndStatus> list) {
            Stream<AgentAndStatus> stream = list.stream();
            if (filter != null) {
                stream = stream.filter(filter::filter);
            }
            return stream;
        }

        private List<ApplicationAgentList> groupByHostName(List<AgentAndStatus> agentList) {
            List<AgentAndStatus> containerList = filteredStream(agentList)
                    .filter(agentAndStatus -> agentAndStatus.getAgentInfo().isContainer())
                    .sorted(Comparator.comparing(agentAndStatus -> agentAndStatus.getAgentInfo().getHostName()))
                    .collect(Collectors.toList());

            Map<String, List<AgentStatusAndLink>> hostNameMap = filteredStream(agentList)
                    .map(this::newAgentInfoAndLink)
                    .collect(Collectors.groupingBy(agent -> agent.getAgentInfo().getHostName()));

            return hostNameMap.entrySet()
                    .stream()
                    .map(entry -> new ApplicationAgentList(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }

        private List<ApplicationAgentList> groupByApplicationName(List<AgentAndStatus> agentList) {

            Map<String, List<AgentAndStatus>> agentIdMap = filteredStream(agentList)
                    .collect(Collectors.groupingBy(agentAndStatus -> agentAndStatus.getAgentInfo().getApplicationName()));

            agentIdMap.values()
                    .forEach(list -> list.sort((o1, o2) -> AgentInfo.AGENT_NAME_ASC_COMPARATOR.compare(o1.getAgentInfo(), o2.getAgentInfo())));

            List<ApplicationAgentList> collect = agentIdMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(this::newApplicationAgentList)
                    .collect(Collectors.toList());
            return collect;
        }


        private ApplicationAgentList newApplicationAgentList(Map.Entry<String, List<AgentAndStatus>> entry) {
            String key = entry.getKey();
            List<AgentStatusAndLink> list = entry.getValue()
                    .stream()
                    .map(this::newAgentInfoAndLink)
                    .collect(Collectors.toList());
            return new ApplicationAgentList(key, list);
        }

        private AgentStatusAndLink newAgentInfoAndLink(AgentAndStatus agentAndStatus) {
            AgentInfo agentInfo = agentAndStatus.getAgentInfo();
            AgentStatus status = agentAndStatus.getStatus();
            List<HyperLink> hyperLinks = hyperLinkFactory.build(LinkSources.from(agentInfo));
            return new AgentStatusAndLink(agentInfo, status, hyperLinks);
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "groupBy=" + groupBy +
                    ", filter=" + filter +
                    ", hyperLinkFactory=" + hyperLinkFactory +
                    ", agentsMap=" + list +
                    '}';
        }
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

    interface GroupByAction<T> {
         GroupingKey<T> extractKey(AgentInfo agentInfo);

        /**
         * Do not use this for sorted set and maps.
         */
        Comparator<AgentInfo> getComparator();
    }

    static class ApplicationNameGroupBy implements GroupByAction<StringGroupingKey> {
        @Override
        public GroupingKey<StringGroupingKey> extractKey(AgentInfo agentInfo) {
            return new StringGroupingKey(agentInfo.getApplicationName());
        }

        @Override
        public Comparator<AgentInfo> getComparator() {
            return AgentInfo.AGENT_NAME_ASC_COMPARATOR;
        }
    }

    static class ContainerGroupBy implements GroupByAction<HostNameContainerGroupingKey> {
        @Override
        public GroupingKey extractKey(AgentInfo agentInfo) {
            return new HostNameContainerGroupingKey(agentInfo.getHostName(), agentInfo.isContainer());
        }

        @Override
        public Comparator<AgentInfo> getComparator() {
            return (agentInfo1, agentInfo2) -> {
                int containerEquals = CONTAINER_FIRST_COMPARE.compare(agentInfo1.isContainer(), agentInfo2.isContainer());
                if (containerEquals != 0) {
                    return containerEquals;
                }
                // reverse start time order if both are containers
                int startTime = Long.compare(agentInfo2.getStartTimestamp(), agentInfo1.getStartTimestamp());
                if (startTime != 0) {
                    return startTime;
                }

                // agent id order if both are not containers
                return AgentInfo.AGENT_NAME_ASC_COMPARATOR.compare(agentInfo1, agentInfo2);
            };
        }
    }


    @VisibleForTesting
    static class HostNameContainerGroupingKey implements GroupingKey<HostNameContainerGroupingKey> {

        public static final String CONTAINER = "Container";

        private final StringGroupingKey hostNameGroupingKey;
        private final boolean isContainer;

        private HostNameContainerGroupingKey(String hostName, boolean isContainer) {
            Objects.requireNonNull(hostName, "hostName");
            if (isContainer) {
                hostName = CONTAINER;
            }
            this.hostNameGroupingKey = new StringGroupingKey(hostName);
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
            final int containerEquals = CONTAINER_FIRST_COMPARE.compare(isContainer, o.isContainer);
            if (containerEquals != 0) {
                return containerEquals;
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

    private static Comparator<Boolean> CONTAINER_FIRST_COMPARE = new Comparator<Boolean>() {
        public int compare(Boolean container1, Boolean container2) {
            if (container1 && container2) {
                return 0;
            }
            if (container1) {
                return -1;
            }
            if (container2) {
                return 1;
            }
            return 0;
        }
    };
}
