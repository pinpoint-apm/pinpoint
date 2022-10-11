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

package com.navercorp.pinpoint.web.vo.tree;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class AgentsList<T> {

    private final String groupName;

    private final List<T> agentSuppliersList;

    public static <T> AgentsList<T> sorted(String groupName, List<T> agentSuppliersList, Comparator<T> sortBy) {
        Objects.requireNonNull(groupName, "groupName");
        Objects.requireNonNull(agentSuppliersList, "agentSuppliersList");
        Objects.requireNonNull(sortBy, "sortBy");

        sort(agentSuppliersList, sortBy);
        return new AgentsList<>(groupName, agentSuppliersList);
    }

    private static <T> void sort(List<T> agentSuppliersList, Comparator<T> comparator) {
        agentSuppliersList.sort(comparator);
    }

    public AgentsList(String groupName, List<T> agentSuppliersList) {
        this.groupName = Objects.requireNonNull(groupName, "groupName");
        this.agentSuppliersList = Objects.requireNonNull(agentSuppliersList, "agentSuppliersList");
    }

    public String getGroupName() {
        return groupName;
    }

    public List<T> getAgentSuppliersList() {
        return agentSuppliersList;
    }

    @Override
    public String toString() {
        return "AgentsList{" +
                "groupName='" + groupName + '\'' +
                ", agentSuppliersList=" + agentSuppliersList +
                '}';
    }
}
