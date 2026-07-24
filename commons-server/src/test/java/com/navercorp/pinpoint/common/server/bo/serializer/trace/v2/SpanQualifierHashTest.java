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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.google.common.hash.Hashing;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpanQualifierHashTest {

    @Test
    void applicationName_isDeterministic() {
        // stored-format compatibility: the same name must always map to the same bucket
        assertThat(SpanQualifierHash.applicationName("order-service"))
                .isEqualTo(SpanQualifierHash.applicationName("order-service"));
    }

    @Test
    void applicationName_nullMapsToZero() {
        assertThat(SpanQualifierHash.applicationName(null)).isEqualTo((byte) 0);
    }

    @Test
    void applicationName_matchesMurmur3LowByte() {
        // pins the algorithm; a change here signals a format-breaking change
        assertThat(SpanQualifierHash.applicationName("order-service")).isEqualTo(murmur3LowByte("order-service"));
        assertThat(SpanQualifierHash.applicationName("payment")).isEqualTo(murmur3LowByte("payment"));
    }

    private static byte murmur3LowByte(String s) {
        return (byte) Hashing.murmur3_32_fixed().hashUnencodedChars(s).asInt();
    }
}
