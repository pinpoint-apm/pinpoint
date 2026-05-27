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
 * Extracts Pinpoint upstream context from the W3C {@code tracestate} header.
 *
 * <p>Expected entry: {@code pp=svc:<parentServiceName>;app:<parentApplicationName>[;type:<serviceTypeCode>]}.
 * Any sub-key may be absent; unknown sub-keys are ignored so the format can be
 * extended later without breaking parsing.</p>
 */
final class PinpointTraceStateParser {

    private PinpointTraceStateParser() {
    }

    /** Parsed Pinpoint sub-keys from a {@code tracestate} header. Any field may be null. */
    record PinpointHeader(String parentServiceName,
                          String parentApplicationName,
                          Short parentApplicationType) {
        boolean isEmpty() {
            return parentServiceName == null
                    && parentApplicationName == null
                    && parentApplicationType == null;
        }
    }

    /**
     * @return parsed header or {@code null} when no usable {@code pp} entry exists
     */
    static PinpointHeader parse(String traceState) {
        if (traceState == null || traceState.isEmpty()) {
            return null;
        }
        for (String entry : traceState.split(",")) {
            int eq = entry.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String key = entry.substring(0, eq).trim();
            if (!OtlpTraceConstants.TRACESTATE_KEY_PINPOINT.equals(key)) {
                continue;
            }
            // W3C tracestate top-level: on duplicate vendor keys, the first list-member
            // wins. Sub-key duplicate semantics inside the value are vendor-defined;
            // parseValue mirrors the same first-wins rule for consistency.
            return parseValue(entry.substring(eq + 1).trim());
        }
        return null;
    }

    private static PinpointHeader parseValue(String value) {
        if (value.isEmpty()) {
            return null;
        }
        String svc = null;
        String app = null;
        Short type = null;
        for (String sub : value.split(";")) {
            int colon = sub.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String subKey = sub.substring(0, colon).trim();
            String subValue = sub.substring(colon + 1).trim();
            if (subValue.isEmpty()) {
                continue;
            }
            // First-wins: only assign when the slot is still empty, so a duplicate
            // sub-key cannot overwrite the earlier value. For 'type' specifically this
            // means "first valid value wins" — a malformed first occurrence leaves the
            // slot null and lets a later well-formed value populate it.
            if (svc == null
                    && OtlpTraceConstants.TRACESTATE_SUBKEY_PARENT_SERVICE_NAME.equals(subKey)) {
                svc = subValue;
            } else if (app == null
                    && OtlpTraceConstants.TRACESTATE_SUBKEY_PARENT_APPLICATION_NAME.equals(subKey)) {
                app = subValue;
            } else if (type == null
                    && OtlpTraceConstants.TRACESTATE_SUBKEY_PARENT_APPLICATION_TYPE.equals(subKey)) {
                type = parseShortOrNull(subValue);
            }
        }
        PinpointHeader header = new PinpointHeader(svc, app, type);
        return header.isEmpty() ? null : header;
    }

    /** Parse a Pinpoint ServiceType code; non-numeric or out-of-short-range returns null. */
    private static Short parseShortOrNull(String value) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < Short.MIN_VALUE || parsed > Short.MAX_VALUE) {
                return null;
            }
            return (short) parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
