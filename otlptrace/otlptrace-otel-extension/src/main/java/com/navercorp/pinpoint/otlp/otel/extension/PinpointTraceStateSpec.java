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

package com.navercorp.pinpoint.otlp.otel.extension;

/**
 * Wire format for the Pinpoint {@code tracestate} vendor entry.
 *
 * <p>Format: {@code pp=svc:<serviceName>;app:<applicationName>[;type:<serviceTypeCode>]}.
 * These constants MUST stay in sync with the collector-side
 * {@code com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceConstants}.
 * A consistency test asserts the literal values on both sides.</p>
 */
final class PinpointTraceStateSpec {

    private PinpointTraceStateSpec() {
    }

    /** W3C tracestate vendor key — 2-letter, lowercase, matches dd/nr/dt/ot convention. */
    static final String KEY = "pp";

    /** Sub-key for upstream serviceName. */
    static final String SUBKEY_SVC = "svc";

    /** Sub-key for upstream applicationName. */
    static final String SUBKEY_APP = "app";

    /** Sub-key for upstream Pinpoint ServiceType code (numeric short). */
    static final String SUBKEY_TYPE = "type";

    /**
     * Build the value portion of the {@code pp} entry. Returns {@code null} if neither
     * {@code app} nor {@code svc} is provided — without either, the entry conveys nothing
     * the collector will use.
     */
    static String buildValue(String svc, String app, Integer type) {
        if ((svc == null || svc.isEmpty()) && (app == null || app.isEmpty())) {
            return null;
        }
        StringBuilder sb = new StringBuilder(64);
        boolean first = true;
        if (svc != null && !svc.isEmpty()) {
            sb.append(SUBKEY_SVC).append(':').append(svc);
            first = false;
        }
        if (app != null && !app.isEmpty()) {
            if (!first) sb.append(';');
            sb.append(SUBKEY_APP).append(':').append(app);
            first = false;
        }
        if (type != null) {
            if (!first) sb.append(';');
            sb.append(SUBKEY_TYPE).append(':').append(type.intValue());
        }
        return sb.toString();
    }
}
