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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the on-the-wire tracestate sub-key strings so a rename on the collector side
 * is caught before it diverges from the {@code otlptrace-otel-extension} module.
 * The sender-side counterpart lives in
 * {@code com.navercorp.pinpoint.otlp.otel.extension.PinpointTraceStateSpecTest}.
 */
class TraceStateSpecConsistencyTest {

    @Test
    void wireFormatConstants_areStable() {
        assertThat(OtlpTraceConstants.TRACESTATE_KEY_PINPOINT).isEqualTo("pp");
        assertThat(OtlpTraceConstants.TRACESTATE_SUBKEY_PARENT_SERVICE_NAME).isEqualTo("svc");
        assertThat(OtlpTraceConstants.TRACESTATE_SUBKEY_PARENT_APPLICATION_NAME).isEqualTo("app");
        assertThat(OtlpTraceConstants.TRACESTATE_SUBKEY_PARENT_APPLICATION_TYPE).isEqualTo("type");
    }
}
