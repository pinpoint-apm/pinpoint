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

package com.navercorp.pinpoint.otlp.trace.collector.mapper.stacktrace;

/**
 * Parses one language's {@code exception.stacktrace} string format into structured frames.
 * Implementations are stateless and line-tolerant: unrecognized lines are skipped, never fatal.
 */
public interface StackTraceParser {

    /** Short stable identifier used as the {@code parser} metric tag. */
    String name();

    /**
     * Content sniffing: whether this parser recognizes the given stacktrace text. Used only when
     * the {@code telemetry.sdk.language} resource attribute is absent or unmapped.
     */
    boolean matches(String stackTrace);

    /** Parses frames into {@code sink}, stopping when the sink refuses further frames. */
    void parse(String stackTrace, StackFrameSink sink);
}
