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

package com.navercorp.pinpoint.collector.receiver;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BindAddressTest {

    @Test
    void validPort() {
        int[] ports = {0, 1, 9991, 65535};
        for (int port : ports) {
            BindAddress bindAddress = new BindAddress("0.0.0.0", port);

            assertThat(bindAddress.getPort()).isEqualTo(port);
        }
    }

    @Test
    void invalidPort() {
        int[] ports = {-1, 65536, Integer.MIN_VALUE, Integer.MAX_VALUE};
        for (int port : ports) {
            assertThatThrownBy(() -> new BindAddress("0.0.0.0", port))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.valueOf(port));
        }
    }

    @Test
    void builder_invalidPort() {
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(-1);

        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void builder_default() {
        BindAddress bindAddress = BindAddress.newBuilder().build();

        assertThat(bindAddress.getIp()).isEqualTo("0.0.0.0");
        assertThat(bindAddress.getPort()).isZero();
    }
}
