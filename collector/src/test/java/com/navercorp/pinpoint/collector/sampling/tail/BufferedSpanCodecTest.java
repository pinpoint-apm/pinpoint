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

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BufferedSpanCodecTest {

    private final BufferedSpanCodec codec = new BufferedSpanCodec();

    @Test
    void roundTripSpan() {
        byte[] proto = new byte[]{1, 2, 3, 4, 5};
        BufferedSpan original = new BufferedSpan(BufferedSpan.Type.SPAN,
                "agent-1", "agent-name", "app-1", 1000L, 2000L, proto);

        byte[] encoded = codec.encode(original);
        BufferedSpan decoded = codec.decode(encoded);

        assertThat(decoded.type()).isEqualTo(BufferedSpan.Type.SPAN);
        assertThat(decoded.agentId()).isEqualTo("agent-1");
        assertThat(decoded.agentName()).isEqualTo("agent-name");
        assertThat(decoded.applicationName()).isEqualTo("app-1");
        assertThat(decoded.agentStartTime()).isEqualTo(1000L);
        assertThat(decoded.requestTime()).isEqualTo(2000L);
        assertThat(decoded.protoBytes()).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void roundTripChunkPreservesType() {
        BufferedSpan original = new BufferedSpan(BufferedSpan.Type.SPAN_CHUNK,
                "a", "n", "app", 1L, 2L, new byte[]{9});
        BufferedSpan decoded = codec.decode(codec.encode(original));
        assertThat(decoded.type()).isEqualTo(BufferedSpan.Type.SPAN_CHUNK);
    }
}
