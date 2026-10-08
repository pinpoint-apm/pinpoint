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

import java.util.Objects;

/**
 * The {@code pinpoint.serviceName} resource attribute names a service that is not registered on
 * the Pinpoint side. Thrown by {@link OtlpResourceIdResolver}; the trace and log receivers turn it
 * into a {@code service_not_found} reject, mirroring the native agent path where
 * {@code GrpcAgentInfoHandler} answers {@code serviceNotFound} and {@code SpanService} discards
 * the spans.
 */
public class OtlpServiceNotFoundException extends RuntimeException {

    private final String serviceName;

    public OtlpServiceNotFoundException(String serviceName) {
        super("service not found. serviceName=" + serviceName);
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
    }

    public String getServiceName() {
        return serviceName;
    }
}
