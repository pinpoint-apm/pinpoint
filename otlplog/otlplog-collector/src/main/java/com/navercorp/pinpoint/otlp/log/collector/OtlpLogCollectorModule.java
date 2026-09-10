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

package com.navercorp.pinpoint.otlp.log.collector;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * OTLP logs receiver co-hosted in the OTLPTRACE collector process. It registers the OTLP
 * {@code LogsService} on the trace collector's gRPC servers and {@code POST /v1/logs} on its servlet
 * port, keeps only the LogRecords that carry {@code exception.*} attributes and stores them as Error
 * Analysis records ({@code ExceptionMetaDataBo}) — the same table and web views as the exceptions
 * mapped from OTel span events.
 *
 * <p>Why this process and not a collector type of its own: OTLP exporters send every signal to one
 * endpoint (gRPC one port, HTTP {@code <base>/v1/<signal>}), the log records correlate with traces
 * through the same {@code trace_id}/{@code span_id} space and need the same resource resolution.
 * This module depends on {@code otlptrace-collector}, so the trace module cannot import it; the
 * collector starter registers it next to {@code OtlpTraceCollectorApp}. The gate therefore requires
 * both module flags: when {@value #ENABLED_KEY} is off the whole module is skipped and the trace
 * collector is unchanged, and when {@value #TRACE_ENABLED_KEY} is off the trace beans this module
 * needs ({@code OtlpExceptionMapper}, the shared gRPC servers) do not exist, so the module is skipped
 * as well instead of failing the OTLPTRACE context on a missing dependency.
 */
@Configuration
@Import({
        OtlpLogCollectorPropertySources.class,
        OtlpLogCollectorGrpcModule.class,
        OtlpLogCollectorHttpModule.class
})
@ComponentScan({
        "com.navercorp.pinpoint.otlp.log.collector.mapper",
        "com.navercorp.pinpoint.otlp.log.collector.service",
        "com.navercorp.pinpoint.otlp.log.collector.controller"
})
@ConditionalOnProperty(name = {OtlpLogCollectorModule.TRACE_ENABLED_KEY, OtlpLogCollectorModule.ENABLED_KEY}, havingValue = "true")
public class OtlpLogCollectorModule {

    public static final String ENABLED_KEY = "pinpoint.modules.collector.otlplog.enabled";
    /** The host module's gate ({@code OtlpTraceCollectorModule}); this module cannot exist without it. */
    public static final String TRACE_ENABLED_KEY = "pinpoint.modules.collector.otlptrace.enabled";

}
