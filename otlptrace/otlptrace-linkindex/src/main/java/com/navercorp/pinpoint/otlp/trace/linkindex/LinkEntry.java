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

import java.util.Objects;

/**
 * One side of a cross-process call.
 *
 * @param kind            SERVER (callee B) or CLIENT (caller A)
 * @param traceId         OTel trace id, 16 bytes
 * @param spanId          SERVER: parentSpanId of B's span / CLIENT: span id of A's CLIENT span
 * @param serviceUid      service uid of the span owner
 * @param applicationName application name of the span owner
 * @param serviceType     application service type code of the span owner
 * @param destinationId   CLIENT only: the destination A called (host:port, rpc service, ...)
 * @param elapsed         SERVER only: elapsed time of B's span in milliseconds
 * @param error           SERVER only: B's span ended with an error
 */
public record LinkEntry(LinkKind kind,
                        byte[] traceId,
                        long spanId,
                        int serviceUid,
                        String applicationName,
                        int serviceType,
                        String destinationId,
                        int elapsed,
                        boolean error) {

    public static final int TRACE_ID_LENGTH = 16;

    public LinkEntry {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(traceId, "traceId");
        Objects.requireNonNull(applicationName, "applicationName");
        if (traceId.length != TRACE_ID_LENGTH) {
            throw new IllegalArgumentException("traceId must be " + TRACE_ID_LENGTH + " bytes:" + traceId.length);
        }
        if (kind == LinkKind.CLIENT) {
            Objects.requireNonNull(destinationId, "destinationId");
        }
    }

    public static LinkEntry server(byte[] traceId, long parentSpanId,
                                   int serviceUid, String applicationName, int serviceType,
                                   int elapsed, boolean error) {
        return new LinkEntry(LinkKind.SERVER, traceId, parentSpanId, serviceUid, applicationName, serviceType, null, elapsed, error);
    }

    public static LinkEntry client(byte[] traceId, long spanId,
                                   int serviceUid, String applicationName, int serviceType,
                                   String destinationId) {
        return new LinkEntry(LinkKind.CLIENT, traceId, spanId, serviceUid, applicationName, serviceType, destinationId, 0, false);
    }

    PairKey pairKey() {
        return PairKey.of(traceId, spanId);
    }
}
