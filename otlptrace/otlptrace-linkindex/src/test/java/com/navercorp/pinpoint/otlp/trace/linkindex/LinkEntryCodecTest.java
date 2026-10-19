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

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LinkEntryCodecTest {

    static byte[] traceId(int seed) {
        byte[] traceId = new byte[16];
        Arrays.fill(traceId, (byte) seed);
        traceId[15] = (byte) (seed * 7);
        return traceId;
    }

    @Test
    void server() {
        LinkEntry entry = LinkEntry.server(traceId(1), 0x1122334455667788L, 7, "p.cart", 1900, 123, true);

        byte[] field = LinkEntryCodec.encodeField(entry);
        assertThat(field).hasSize(25);
        assertThat(field[0]).isEqualTo((byte) 'S');

        LinkEntry decoded = LinkEntryCodec.decode(field, LinkEntryCodec.encodeValue(entry));
        assertThat(decoded.kind()).isEqualTo(LinkKind.SERVER);
        assertThat(decoded.traceId()).isEqualTo(entry.traceId());
        assertThat(decoded.spanId()).isEqualTo(0x1122334455667788L);
        assertThat(decoded.serviceUid()).isEqualTo(7);
        assertThat(decoded.applicationName()).isEqualTo("p.cart");
        assertThat(decoded.serviceType()).isEqualTo(1900);
        assertThat(decoded.elapsed()).isEqualTo(123);
        assertThat(decoded.error()).isTrue();
        assertThat(decoded.destinationId()).isNull();
    }

    @Test
    void client() {
        LinkEntry entry = LinkEntry.client(traceId(2), -5L, -1, "p.checkout", 1900, "cart:7070");

        LinkEntry decoded = LinkEntryCodec.decode(LinkEntryCodec.encodeField(entry), LinkEntryCodec.encodeValue(entry));
        assertThat(decoded.kind()).isEqualTo(LinkKind.CLIENT);
        assertThat(decoded.spanId()).isEqualTo(-5L);
        assertThat(decoded.serviceUid()).isEqualTo(-1);
        assertThat(decoded.applicationName()).isEqualTo("p.checkout");
        assertThat(decoded.destinationId()).isEqualTo("cart:7070");
    }

    @Test
    void serverAndClientOfOneCallShareTheFieldExceptTheKind() {
        LinkEntry server = LinkEntry.server(traceId(3), 42, 0, "b", 1900, 1, false);
        LinkEntry client = LinkEntry.client(traceId(3), 42, 0, "a", 1900, "b:80");

        byte[] serverField = LinkEntryCodec.encodeField(server);
        byte[] clientField = LinkEntryCodec.encodeField(client);
        assertThat(Arrays.copyOfRange(serverField, 1, 25)).isEqualTo(Arrays.copyOfRange(clientField, 1, 25));
    }

    @Test
    void invalid() {
        LinkEntry entry = LinkEntry.client(traceId(4), 1, 0, "a", 1900, "b:80");
        byte[] field = LinkEntryCodec.encodeField(entry);
        byte[] value = LinkEntryCodec.encodeValue(entry);

        assertThatThrownBy(() -> LinkEntryCodec.decode(Arrays.copyOf(field, 24), value))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LinkEntryCodec.decode(field, new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LinkEntryCodec.decode(field, Arrays.copyOf(value, 4)))
                .isInstanceOf(IllegalArgumentException.class);

        byte[] badVersion = value.clone();
        badVersion[0] = 9;
        assertThatThrownBy(() -> LinkEntryCodec.decode(field, badVersion))
                .isInstanceOf(IllegalArgumentException.class);

        byte[] badKind = field.clone();
        badKind[0] = 'X';
        assertThatThrownBy(() -> LinkEntryCodec.decode(badKind, value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void traceIdMustBe16Bytes() {
        assertThatThrownBy(() -> LinkEntry.server(new byte[8], 1, 0, "b", 1900, 1, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
