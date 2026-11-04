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

package com.navercorp.pinpoint.otlp.trace.collector;

import com.navercorp.pinpoint.datasource.MainDataSourceConfiguration;
import com.navercorp.pinpoint.datasource.MainDataSourcePropertySource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.otlp.OtlpMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration(
        exclude = {
                OtlpMetricsExportAutoConfiguration.class
        },
        // The OTLP trace ingestion servlet (POST /v1/traces on the HTTP port) is an internal,
        // unauthenticated endpoint. Exclude servlet security auto-config defensively: if
        // spring-boot-security reaches the classpath (e.g. transitively in a downstream deploy),
        // the default SecurityFilterChain would otherwise reject every HTTP request with 401
        // while gRPC ingestion — which bypasses the servlet filter chain — kept working,
        // silently dropping OTLP/HTTP exporters (e.g. .NET services).
        // Referenced by name: the classes are absent from this module's classpath.
        excludeName = {
                "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
                "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration",
                "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
                "org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration"
        })
@Import({
        OtlpTraceCollectorModule.class,

        MainDataSourcePropertySource.class,
        MainDataSourceConfiguration.class
})
public class OtlpTraceCollectorApp {
}
