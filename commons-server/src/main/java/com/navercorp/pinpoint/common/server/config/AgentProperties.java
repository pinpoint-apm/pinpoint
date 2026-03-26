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

package com.navercorp.pinpoint.common.server.config;

import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

public class AgentProperties {

    // Service types that require span statistics check instead of ping-based status filtering
    @Value("${pinpoint.agent.statistics-check-service-types:1220,1400,1700}")
    private Set<Integer> statisticsCheckServiceTypeCodes; // OTLP(1220), Node(1400), Python(1700)

    // Service types whose agents do not send serviceType in the gRPC header (handled as serviceTypeCode -1)
    @Value("${pinpoint.agent.missing-header-service-types:1400,1700,1800,1550}")
    private Set<Integer> missingHeaderServiceTypeCodes; // Node(1400), Python(1700), Go(1800), Envoy(1550)

    public Set<Integer> getStatisticsCheckServiceTypeCodes() {
        return statisticsCheckServiceTypeCodes;
    }

    public Set<Integer> getMissingHeaderServiceTypeCodes() {
        return missingHeaderServiceTypeCodes;
    }
}