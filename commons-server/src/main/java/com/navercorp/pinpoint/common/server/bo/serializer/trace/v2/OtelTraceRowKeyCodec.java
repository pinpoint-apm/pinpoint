/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;

import java.util.Arrays;

/**
 * Trace row key layout for OTel spans.
 * <pre>
 * salt(1) + trace_id(16)
 * </pre>
 * The salt slot is fixed to a single byte; this layout does not honor other salt key sizes.
 * On read the layout is recognized by total length via {@link #matches(byte[], int)} —
 * it can never collide with the fixed 41-byte {@link PinpointTraceRowKeyCodec} layout.
 *
 * @author Woonduk Kang(emeroad)
 */
public final class OtelTraceRowKeyCodec {

    public static final int TRACE_ID_LEN = PinpointConstants.OPENTELEMETRY_TRACE_ID_LEN;

    public static final int SALT_KEY_SIZE = 1;

    private OtelTraceRowKeyCodec() {
    }

    public static boolean matches(byte[] rowKey, int saltKeySize) {
        return rowKey.length == saltKeySize + TRACE_ID_LEN;
    }

    public static byte[] encode(OtelServerTraceId traceId) {
        byte[] traceIdBytes = traceId.getId();
        byte[] rowKey = new byte[SALT_KEY_SIZE + TRACE_ID_LEN];
        System.arraycopy(traceIdBytes, 0, rowKey, SALT_KEY_SIZE, TRACE_ID_LEN);
        return rowKey;
    }

    public static OtelServerTraceId decode(byte[] rowKey, int saltKeySize) {
        byte[] traceId = Arrays.copyOfRange(rowKey, saltKeySize, saltKeySize + TRACE_ID_LEN);
        return new OtelServerTraceId(traceId);
    }
}
