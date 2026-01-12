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

package com.navercorp.pinpoint.otlp.trace.collector;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;


@PropertySources({
        @PropertySource(name = "CollectorAppPropertySources", value = { OtlpTraceCollectorPropertySources.COLLECTOR_ROOT, OtlpTraceCollectorPropertySources.COLLECTOR_PROFILE}),
        @PropertySource(name = "CollectorAppPropertySources-GRPC", value = { OtlpTraceCollectorPropertySources.GRPC_ROOT, OtlpTraceCollectorPropertySources.GRPC_PROFILE}),
})
public final class OtlpTraceCollectorPropertySources {
    public static final String GRPC_ROOT = "classpath:pinpoint-collector-grpc-root.properties";
    public static final String GRPC_PROFILE = "classpath:profiles/${pinpoint.profiles.active:local}/pinpoint-collector-grpc.properties";

    public static final String COLLECTOR_ROOT = "classpath:pinpoint-collector-root.properties";
    public static final String COLLECTOR_PROFILE = "classpath:profiles/${pinpoint.profiles.active:local}/pinpoint-collector.properties";

    private OtlpTraceCollectorPropertySources() {
    }
}
