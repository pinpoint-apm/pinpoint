/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.config;

import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

public class AgentListProperties {

    @Value("${pinpoint.agent.list.filter.statisticsExistence.serviceTypes:1400,1700}") // node(1400), python(1700)
    private Set<Integer> filterStatisticsExistenceServiceTypeCodes;

    @Value("${pinpoint.agent.list.filter.lastStatus.exclude.serviceTypes:1220,1400,1700}") // otlp(1220), node(1400), python(1700)
    private Set<Integer> filterLastStatusExcludeServiceTypeCodes;

    public Set<Integer> getFilterStatisticsExistenceServiceTypeCodes() {
        return filterStatisticsExistenceServiceTypeCodes;
    }

    public Set<Integer> getFilterLastStatusExcludeServiceTypeCodes() {
        return filterLastStatusExcludeServiceTypeCodes;
    }
}
