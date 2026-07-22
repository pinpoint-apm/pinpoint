/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.virtualapplication;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.applicationmap.virtualapplication.VirtualApplicationProperties.ApplicationRef;
import com.navercorp.pinpoint.web.applicationmap.virtualapplication.VirtualApplicationProperties.VirtualApplicationRule;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VirtualApplicationResolver {

    private static final String SERVICE_TYPE_NAME = ServiceType.SERVICE.getName();
    private static final VirtualApplicationResolver EMPTY = new VirtualApplicationResolver(Map.of());

    private final Map<String, List<Application>> table;

    VirtualApplicationResolver(Map<String, List<Application>> table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public static VirtualApplicationResolver emptyResolver() {
        return EMPTY;
    }

    public static VirtualApplicationResolver of(List<VirtualApplicationRule> rules, ApplicationValidator validator) {
        Objects.requireNonNull(rules, "rules");
        Objects.requireNonNull(validator, "validator");

        Map<String, List<Application>> table = new LinkedHashMap<>();
        for (VirtualApplicationRule rule : rules) {
            String virtualServiceName = rule.getVirtualServiceName();
            if (!StringUtils.hasLength(virtualServiceName)) {
                throw new IllegalArgumentException("virtualServiceName must not be empty");
            }
            List<ApplicationRef> members = rule.getMembers();
            if (CollectionUtils.isEmpty(members)) {
                throw new IllegalArgumentException("members must not be empty for virtualServiceName=" + virtualServiceName);
            }
            List<Application> applications = members.stream()
                    .map(m -> validator.newApplication(m.getName(), m.getServiceType()))
                    .toList();
            if (table.put(virtualServiceName, applications) != null) {
                throw new IllegalArgumentException("duplicate virtualServiceName=" + virtualServiceName);
            }
        }
        return new VirtualApplicationResolver(Map.copyOf(table));
    }

    public List<Application> resolve(Application input) {
        Objects.requireNonNull(input, "input");
        if (!SERVICE_TYPE_NAME.equals(input.getServiceType().getName())) {
            return List.of(input);
        }
        List<Application> expanded = table.get(input.getApplicationName());
        if (expanded != null) {
            return expanded;
        }
        return List.of(input);
    }
}