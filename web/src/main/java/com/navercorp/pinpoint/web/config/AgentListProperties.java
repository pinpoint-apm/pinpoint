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

    @Value("${pinpoint.web.agents.check.statistics.serviceTypes:1220,1400,1700}") // otlp(1220), node(1400), python(1700)
    private Set<Integer> checkStatisticsServiceTypeCodes;

    @Value("${pinpoint.web.agents.filter.updateTime.exclude.serviceTypes:1800,1550}") //go(1800), envoy(1550)
    private Set<Integer> filterUpdateTimeExcludeServiceTypeCodes;

    @Value("${pinpoint.web.agents.filter.updateTime.thresholdMillis:86400000}") // default 1 day
    private long filterUpdateTimeThresholdMillis;

    @Value("${pinpoint.web.agents.filter.lastStatus.exclude.serviceTypes:}")
    private Set<Integer> filterLastStatusExcludeServiceTypeCodes;

    @Value("${pinpoint.web.agents.filter.lastStatus.thresholdMillis:0}") // default 0
    private long filterLastStatusThresholdMillis;

    public Set<Integer> getCheckStatisticsServiceTypeCodes() {
        return checkStatisticsServiceTypeCodes;
    }

    public Set<Integer> getFilterUpdateTimeExcludeServiceTypeCodes() {
        return filterUpdateTimeExcludeServiceTypeCodes;
    }

    public long getFilterUpdateTimeThresholdMillis() {
        return filterUpdateTimeThresholdMillis;
    }

    public Set<Integer> getFilterLastStatusExcludeServiceTypeCodes() {
        return filterLastStatusExcludeServiceTypeCodes;
    }

    public long getFilterLastStatusThresholdMillis() {
        return filterLastStatusThresholdMillis;
    }
}
