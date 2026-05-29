/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.sampling.tail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Length-prefixed binary serialization of BufferedSpan to/from byte[] for the Redis buffer.
 */
public class BufferedSpanCodec {

    private static final BufferedSpan.Type[] TYPES = BufferedSpan.Type.values();

    public byte[] encode(BufferedSpan span) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(span.type().ordinal());
            out.writeUTF(nullToEmpty(span.agentId()));
            out.writeUTF(nullToEmpty(span.agentName()));
            out.writeUTF(nullToEmpty(span.applicationName()));
            out.writeLong(span.agentStartTime());
            out.writeLong(span.requestTime());
            out.writeInt(span.protoBytes().length);
            out.write(span.protoBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return baos.toByteArray();
    }

    public BufferedSpan decode(byte[] bytes) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            BufferedSpan.Type type = TYPES[in.readByte() & 0xFF];
            String agentId = in.readUTF();
            String agentName = in.readUTF();
            String applicationName = in.readUTF();
            long agentStartTime = in.readLong();
            long requestTime = in.readLong();
            int len = in.readInt();
            byte[] proto = new byte[len];
            in.readFully(proto);
            return new BufferedSpan(type, agentId, agentName, applicationName, agentStartTime, requestTime, proto);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
