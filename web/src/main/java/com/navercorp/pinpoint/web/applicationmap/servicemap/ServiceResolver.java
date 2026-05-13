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

package com.navercorp.pinpoint.web.applicationmap.servicemap;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.applicationmap.servicemap.ServiceMappingProperties.ServiceMappingRule;
import com.navercorp.pinpoint.web.vo.Service;

import java.util.List;
import java.util.Objects;

public class ServiceResolver {

    private static final ServiceResolver EMPTY = new ServiceResolver(List.of());

    private final ServiceMappingRule[] rules;

    public ServiceResolver(List<ServiceMappingRule> rules) {
        Objects.requireNonNull(rules, "rules");
        this.rules = rules.toArray(new ServiceMappingRule[0]);
    }


    public static ServiceResolver emptyResolver() {
        return EMPTY;
    }

    public Service resolve(String applicationName, Service defaultService) {
        for (ServiceMappingRule rule : rules) {
            List<String> prefixes = rule.getPrefix();
            if (CollectionUtils.isEmpty(prefixes)) {
                continue;
            }
            for (String prefix : prefixes) {
                if (applicationName.startsWith(prefix)) {
                    return new Service(rule.getServiceName(), rule.getServiceUid());
                }
            }
        }
        return defaultService;
    }
}