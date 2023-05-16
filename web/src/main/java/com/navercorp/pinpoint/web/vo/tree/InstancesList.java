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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class InstancesList<T> {

    private final String groupName;

    private final List<T> instancesList;

    public static <T> InstancesList<T> sorted(String groupName, List<T> instancesList, Comparator<T> sortBy) {
        Objects.requireNonNull(groupName, "groupName");
        Objects.requireNonNull(instancesList, "instancesList");
        Objects.requireNonNull(sortBy, "sortBy");

        sort(instancesList, sortBy);
        return new InstancesList<>(groupName, instancesList);
    }

    private static <T> void sort(List<T> agentSuppliersList, Comparator<T> comparator) {
        agentSuppliersList.sort(comparator);
    }

    public InstancesList(String groupName, List<T> instancesList) {
        this.groupName = Objects.requireNonNull(groupName, "groupName");
        this.instancesList = Objects.requireNonNull(instancesList, "agentSuppliersList");
    }

    @JsonProperty
    public String getGroupName() {
        return groupName;
    }

    @JsonProperty
    public List<T> getInstancesList() {
        return instancesList;
    }

    @Override
    public String toString() {
        return "InstancesList{" +
                "groupName='" + groupName + '\'' +
                ", instancesList=" + instancesList +
                '}';
    }
}
