/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.common.pinot;

/**
 * @author intr3p1d
 */
public enum PinotColumns {
    TRANSACTION_ID("transactionId"),
    SPAN_ID("spanId"),
    EXCEPTION_ID("exceptionId"),
    APPLICATION_SERVICE_TYPE("applicationServiceType"),
    APPLICATION_NAME("applicationName"),
    AGENT_ID("agentId"),
    URI_TEMPLATE("uriTemplate"),
    ERROR_CLASS_NAME("errorClassName"),
    ERROR_MESSAGE("errorMessage"),
    EXCEPTION_DEPTH("exceptionDepth"),
    STACK_TRACE_CLASS_NAME("stackTraceClassName"),
    STACK_TRACE_FILE_NAME("stackTraceFileName"),
    STACK_TRACE_LINE_NUMBER("stackTraceLineNumber"),
    STACK_TRACE_METHOD_NAME("stackTraceMethodName"),
    STACK_TRACE_HASH("stackTraceHash");

    private final String name;

    PinotColumns(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
