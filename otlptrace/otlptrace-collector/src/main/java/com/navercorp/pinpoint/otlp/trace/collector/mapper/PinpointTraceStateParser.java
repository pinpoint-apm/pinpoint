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

import org.jspecify.annotations.Nullable;

/**
 * Extracts Pinpoint upstream context from the W3C {@code tracestate} header.
 *
 * <p>Expected entry: {@code pp=svc:<parentServiceName>;app:<parentApplicationName>;type:<serviceTypeCode>}.
 * All sub-keys are required; unknown sub-keys are ignored so the format can be
 * extended later without breaking parsing.</p>
 */
final class PinpointTraceStateParser {

    private static final String PINPOINT_ENTRY_PREFIX = OtlpTraceConstants.TRACESTATE_KEY_PINPOINT + "=";

    private PinpointTraceStateParser() {
    }

    /** Parsed Pinpoint sub-keys from a {@code tracestate} header. */
    record PinpointHeader(String parentServiceName,
                          String parentApplicationName,
                          Integer parentApplicationType) {
        static @Nullable PinpointHeader ofNullable(@Nullable String parentServiceName,
                                                   @Nullable String parentApplicationName,
                                                   @Nullable Integer parentApplicationType) {
            if (parentServiceName == null || parentApplicationName == null || parentApplicationType == null) {
                return null;
            }
            return new PinpointHeader(parentServiceName, parentApplicationName, parentApplicationType);
        }
    }

    /**
     * @return parsed header or {@code null} when no usable {@code pp} entry exists
     */
    static @Nullable PinpointHeader parse(@Nullable String traceState) {
        if (traceState == null || traceState.isEmpty()) {
            return null;
        }
        String pinpointEntryValue = findPinpointEntryValue(traceState, PINPOINT_ENTRY_PREFIX);
        if (pinpointEntryValue == null) {
            return null;
        }
        return parseValue(pinpointEntryValue);
    }

    private static @Nullable String findPinpointEntryValue(String traceState, String entryPrefix) {
        for (String entry : traceState.split(",")) {
            if (!entry.startsWith(entryPrefix)) {
                continue;
            }
            // W3C tracestate top-level: on duplicate vendor keys, the first list-member
            // wins. Sub-key duplicate semantics inside the value are vendor-defined;
            // parseValue mirrors the same first-wins rule for consistency.
            return entry.substring(entryPrefix.length());
        }
        return null;
    }

    private static @Nullable PinpointHeader parseValue(String value) {
        if (value.isEmpty()) {
            return null;
        }
        String svc = null;
        String app = null;
        Integer type = null;
        for (String sub : value.split(";")) {
            int colon = sub.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String subKey = sub.substring(0, colon);
            String subValue = sub.substring(colon + 1);
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
                type = parseIntegerOrNull(subValue);
            }
        }
        return PinpointHeader.ofNullable(svc, app, type);
    }

    /** Parse a Pinpoint ServiceType code; non-numeric or out-of-int-range returns null. */
    private static @Nullable Integer parseIntegerOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
