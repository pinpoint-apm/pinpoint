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

package com.navercorp.pinpoint.common.server.bo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionInfoTest {

    @Test
    void otelClassName_present() {
        assertThat(ExceptionInfo.otelClassName("java.io.IOException:disk full"))
                .isEqualTo("java.io.IOException");
    }

    @Test
    void otelClassName_emptyPrefix_isNull() {
        // message-only encoding starts with the delimiter → no class name
        assertThat(ExceptionInfo.otelClassName(":Connection refused")).isNull();
    }

    @Test
    void otelClassName_noDelimiter_isNull() {
        assertThat(ExceptionInfo.otelClassName("no delimiter")).isNull();
    }

    @Test
    void otelClassName_null_isNull() {
        assertThat(ExceptionInfo.otelClassName(null)).isNull();
    }

    @Test
    void otelMessageBody_present() {
        assertThat(ExceptionInfo.otelMessageBody("java.io.IOException:disk full"))
                .isEqualTo("disk full");
    }

    @Test
    void otelMessageBody_keepsColonsInMessage() {
        // split on the FIRST delimiter only; the message may contain further colons
        assertThat(ExceptionInfo.otelMessageBody(":Connection refused: localhost:8080"))
                .isEqualTo("Connection refused: localhost:8080");
    }

    @Test
    void otelMessageBody_emptyMessage() {
        assertThat(ExceptionInfo.otelMessageBody("java.io.IOException:")).isEmpty();
    }

    @Test
    void otelMessageBody_noDelimiter_returnsWhole() {
        assertThat(ExceptionInfo.otelMessageBody("plain")).isEqualTo("plain");
    }
}
