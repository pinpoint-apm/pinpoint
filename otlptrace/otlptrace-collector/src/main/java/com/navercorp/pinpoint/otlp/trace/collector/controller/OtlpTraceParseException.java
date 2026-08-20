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

package com.navercorp.pinpoint.otlp.trace.collector.controller;

/**
 * A request body that cannot be decoded into an {@code ExportTraceServiceRequest} (malformed
 * OTLP/JSON, invalid hex ID, proto3 JSON mapping violation). Rendered by the controller as
 * HTTP 400 with a {@code google.rpc.Status} body per the OTLP/HTTP failure spec.
 */
public class OtlpTraceParseException extends RuntimeException {

    public OtlpTraceParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
