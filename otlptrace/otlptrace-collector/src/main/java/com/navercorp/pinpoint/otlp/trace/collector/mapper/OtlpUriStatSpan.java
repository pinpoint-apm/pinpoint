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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

/**
 * A single entry-point span's contribution to URI stat, captured during trace mapping while the
 * OTel route template attributes (http.route, next.route, micrometer uri) are still available. Deliberately free of any uristat-module type so the
 * always-on {@link OtlpTraceMapper} does not depend on the optional uristat collector; aggregation
 * into UriStat records happens later in OtlpUriStatService, which only loads when the uristat module
 * is enabled.
 */
public class OtlpUriStatSpan {
    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final String uri;
    private final long startTime;
    private final int elapsed;
    private final boolean error;

    public OtlpUriStatSpan(String serviceName, String applicationName, String agentId,
                           String uri, long startTime, int elapsed, boolean error) {
        this.serviceName = serviceName;
        this.applicationName = applicationName;
        this.agentId = agentId;
        this.uri = uri;
        this.startTime = startTime;
        this.elapsed = elapsed;
        this.error = error;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getUri() {
        return uri;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getElapsed() {
        return elapsed;
    }

    public boolean isError() {
        return error;
    }
}
