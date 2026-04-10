/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.timeseries.time;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimestampTest {

    @Test
    void of() {
        Timestamp timestamp = Timestamp.ofEpochMilli(1000L);
        assertThat(timestamp.getEpochMillis()).isEqualTo(1000L);
    }

    @Test
    void parseEpochMillis() {
        Timestamp timestamp = Timestamp.valueOf("1690000000000");
        assertThat(timestamp.getEpochMillis()).isEqualTo(1690000000000L);
    }

    @Test
    void parseIso8601WithOffset() {
        String iso = "2025-07-23T10:42:03.688390+09:00";
        Timestamp timestamp = Timestamp.valueOf(iso);

        OffsetDateTime expected = OffsetDateTime.of(2025, 7, 23, 10, 42, 3, 688390000, ZoneOffset.ofHours(9));
        assertThat(timestamp.getEpochMillis()).isEqualTo(expected.toInstant().toEpochMilli());
    }

    @Test
    void parseIso8601Utc() {
        String iso = "2025-07-23T01:42:03Z";
        Timestamp timestamp = Timestamp.valueOf(iso);

        Instant expected = Instant.parse("2025-07-23T01:42:03Z");
        assertThat(timestamp.getEpochMillis()).isEqualTo(expected.toEpochMilli());
    }

    @Test
    void parseIso8601SeoulTimezone() {
        String iso = "2025-07-23T10:42:03+09:00";
        Timestamp timestamp = Timestamp.valueOf(iso);

        // 서울 시간 10:42:03 = UTC 01:42:03
        Instant expected = Instant.parse("2025-07-23T01:42:03Z");
        assertThat(timestamp.getEpochMillis()).isEqualTo(expected.toEpochMilli());
    }

    @Test
    void parseIso8601SeoulTimezoneWithMillis() {
        String iso = "2025-07-23T10:42:03.123+09:00";
        Timestamp timestamp = Timestamp.valueOf(iso);

        Instant expected = Instant.parse("2025-07-23T01:42:03.123Z");
        assertThat(timestamp.getEpochMillis()).isEqualTo(expected.toEpochMilli());
    }

    @Test
    void toInstant() {
        long millis = 1690000000000L;
        Timestamp timestamp = Timestamp.ofEpochMilli(millis);
        assertThat(timestamp.toInstant()).isEqualTo(Instant.ofEpochMilli(millis));
    }

    @Test
    void parseNull() {
        assertThatThrownBy(() -> Timestamp.valueOf(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void parseInvalidString() {
        assertThatThrownBy(() -> Timestamp.valueOf("invalid"))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    void ofNegative() {
        assertThatThrownBy(() -> Timestamp.ofEpochMilli(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ofZero() {
        Timestamp timestamp = Timestamp.ofEpochMilli(0L);
        assertThat(timestamp.getEpochMillis()).isEqualTo(0L);
    }

    @Test
    void equals() {
        Timestamp a = Timestamp.ofEpochMilli(1000L);
        Timestamp b = Timestamp.ofEpochMilli(1000L);
        Timestamp c = Timestamp.ofEpochMilli(2000L);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
    }
}
