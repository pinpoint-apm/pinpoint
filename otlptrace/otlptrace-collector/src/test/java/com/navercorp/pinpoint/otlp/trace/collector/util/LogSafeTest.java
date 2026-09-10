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

package com.navercorp.pinpoint.otlp.trace.collector.util;

import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogSafeTest {

    @Test
    void controlCharacters_areEscaped_soALineCannotBeForged() {
        String forged = "app\r\n2026-09-10 WARN fake line\tdone";

        String safe = LogSafe.value(forged);

        assertThat(safe).doesNotContain("\r").doesNotContain("\n").doesNotContain("\t");
        assertThat(safe).isEqualTo("app\\u000d\\u000a2026-09-10 WARN fake line\\u0009done\\u007f");
    }

    @Test
    void longValues_areAbbreviated_withTheOriginalLength() {
        String huge = "x".repeat(10_000);

        String safe = LogSafe.value(huge, 16);

        assertThat(safe).isEqualTo("x".repeat(16) + "...(len=10000)");
        assertThat(LogSafe.value(huge)).hasSize(LogSafe.DEFAULT_MAX_LENGTH + "...(len=10000)".length());
    }

    @Test
    void plainValues_passThrough() {
        assertThat(LogSafe.value("order-api")).isEqualTo("order-api");
        assertThat(LogSafe.value(null)).isEqualTo("null");
    }

    /** The resource-id validators echo the rejected value; it must arrive escaped and bounded. */
    @Test
    void validationFailureMessages_carryTheSanitizedValue() {
        String hostile = "bad\nname" + "!".repeat(1000);
        Map<String, AttributeValue> attributes = Map.of("service.name", AttributeValue.of(hostile));

        assertThatThrownBy(() -> OtlpTraceMapperUtils.getId(attributes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid applicationName=bad\\u000aname")
                .hasMessageContaining("...(len=1008)")
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain("\n").hasSizeLessThan(400));
    }
}
