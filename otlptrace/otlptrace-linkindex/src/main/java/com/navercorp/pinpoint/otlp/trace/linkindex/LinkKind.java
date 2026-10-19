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

package com.navercorp.pinpoint.otlp.trace.linkindex;

/**
 * Side of a cross-process call recorded in the link index.
 * <ul>
 *     <li>{@link #SERVER}: the callee's SERVER span (B), keyed by its parentSpanId.</li>
 *     <li>{@link #CLIENT}: the caller's RPC CLIENT span event (A), keyed by its own span id (nextSpanId).</li>
 * </ul>
 * Both keys are the OTel span id of the caller's CLIENT span, so the two entries of one call
 * share the field key except for the kind prefix.
 */
public enum LinkKind {
    SERVER((byte) 'S'),
    CLIENT((byte) 'C');

    private final byte code;

    LinkKind(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static LinkKind fromCode(byte code) {
        for (LinkKind kind : values()) {
            if (kind.code == code) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown link kind code:" + code);
    }
}
