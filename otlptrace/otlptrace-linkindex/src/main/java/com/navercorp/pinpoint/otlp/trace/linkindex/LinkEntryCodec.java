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

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Arrays;

/**
 * Binary encoding of a {@link LinkEntry} as a Redis hash field and value.
 * <pre>
 * field: kind(1) | traceId(16) | spanId(8)                          = 25 bytes
 * value: version(1) | serviceUid(svint) | applicationName(prefixed) | serviceType(svint)
 *        SERVER: elapsed(svint) | error(1)
 *        CLIENT: destinationId(prefixed)
 * </pre>
 */
public final class LinkEntryCodec {

    static final byte VERSION = 1;
    static final int FIELD_LENGTH = 1 + LinkEntry.TRACE_ID_LENGTH + 8;

    private LinkEntryCodec() {
    }

    public static byte[] encodeField(LinkEntry entry) {
        final byte[] field = new byte[FIELD_LENGTH];
        field[0] = entry.kind().getCode();
        System.arraycopy(entry.traceId(), 0, field, 1, LinkEntry.TRACE_ID_LENGTH);
        BytesUtils.writeLong(entry.spanId(), field, 1 + LinkEntry.TRACE_ID_LENGTH);
        return field;
    }

    public static byte[] encodeValue(LinkEntry entry) {
        final Buffer buffer = new AutomaticBuffer(64);
        buffer.putByte(VERSION);
        buffer.putSVInt(entry.serviceUid());
        buffer.putPrefixedString(entry.applicationName());
        buffer.putSVInt(entry.serviceType());
        if (entry.kind() == LinkKind.SERVER) {
            buffer.putSVInt(entry.elapsed());
            buffer.putBoolean(entry.error());
        } else {
            buffer.putPrefixedString(entry.destinationId());
        }
        return buffer.copyBuffer();
    }

    /**
     * @throws IllegalArgumentException if the field or value is not a valid encoding
     */
    public static LinkEntry decode(byte[] field, byte[] value) {
        if (field == null || field.length != FIELD_LENGTH) {
            throw new IllegalArgumentException("Invalid field length");
        }
        if (value == null || value.length == 0) {
            throw new IllegalArgumentException("Empty value");
        }
        final LinkKind kind = LinkKind.fromCode(field[0]);
        final byte[] traceId = Arrays.copyOfRange(field, 1, 1 + LinkEntry.TRACE_ID_LENGTH);
        final long spanId = BytesUtils.bytesToLong(field, 1 + LinkEntry.TRACE_ID_LENGTH);

        try {
            final Buffer buffer = new FixedBuffer(value);
            final byte version = buffer.readByte();
            if (version != VERSION) {
                throw new IllegalArgumentException("Unsupported version:" + version);
            }
            final int serviceUid = buffer.readSVInt();
            final String applicationName = buffer.readPrefixedString();
            final int serviceType = buffer.readSVInt();
            if (applicationName == null) {
                throw new IllegalArgumentException("Missing applicationName");
            }
            if (kind == LinkKind.SERVER) {
                final int elapsed = buffer.readSVInt();
                final boolean error = buffer.readBoolean();
                return LinkEntry.server(traceId, spanId, serviceUid, applicationName, serviceType, elapsed, error);
            }
            final String destinationId = buffer.readPrefixedString();
            if (destinationId == null) {
                throw new IllegalArgumentException("Missing destinationId");
            }
            return LinkEntry.client(traceId, spanId, serviceUid, applicationName, serviceType, destinationId);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Truncated value", e);
        }
    }
}
