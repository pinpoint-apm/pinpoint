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

package com.navercorp.pinpoint.otlp.log.collector.mapper;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.SPAN_ID;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.exceptionRecord;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.kv;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.plainRecord;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.record;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionLogSelectorTest {

    private final ExceptionLogSelector empty = new ExceptionLogSelector(List.of(), List.of());

    @Test
    void anyExceptionPrefixedKey_selects() {
        assertThat(empty.hasExceptionAttribute(exceptionRecord(SPAN_ID, "java.lang.IllegalStateException", "boom", null).build())).isTrue();
        // Go's otelslog: type + message only, no stacktrace.
        assertThat(empty.hasExceptionAttribute(record(SPAN_ID, kv("exception.type", "*errors.errorString")).build())).isTrue();
        // Any exception.* key counts, even a vendor extension, since the value is never parsed here.
        assertThat(empty.hasExceptionAttribute(record(SPAN_ID, kv("exception.escaped", "false")).build())).isTrue();
    }

    @Test
    void ordinaryLogLine_isNotSelected() {
        assertThat(empty.hasExceptionAttribute(plainRecord(SPAN_ID, "request served").build())).isFalse();
        // error.type alone is a span-style attribute, not an exception record.
        assertThat(empty.hasExceptionAttribute(record(SPAN_ID, kv("error.type", "500")).build())).isFalse();
        // Similar-looking keys must not match: prefix is "exception." with the dot.
        assertThat(empty.hasExceptionAttribute(record(SPAN_ID, kv("exceptional", "yes"), kv("exceptions_total", "3")).build())).isFalse();
    }

    @Test
    void blacklist_matchesScopeOrApplicationPrefix() {
        ExceptionLogSelector selector = new ExceptionLogSelector(
                List.of("io.opentelemetry.exporter", " org.apache.kafka "),
                List.of("otel-collector"));

        assertThat(selector.isBlacklisted("io.opentelemetry.exporter.internal", "app-1")).isTrue();
        assertThat(selector.isBlacklisted("org.apache.kafka.clients", "app-1")).isTrue();
        assertThat(selector.isBlacklisted("com.example.App", "otel-collector-gateway")).isTrue();
        assertThat(selector.isBlacklisted("com.example.App", "app-1")).isFalse();
        assertThat(selector.isBlacklisted(null, null)).isFalse();
    }

    @Test
    void blankEntries_areIgnored_soAnEmptyPropertyBlacklistsNothing() {
        // "${key:}" yields an empty string element in some binders; it must not match every value.
        ExceptionLogSelector selector = new ExceptionLogSelector(List.of(""), List.of(" "));

        assertThat(selector.scopePrefixes()).isEmpty();
        assertThat(selector.applicationPrefixes()).isEmpty();
        assertThat(selector.isBlacklisted("anything", "anything")).isFalse();
    }

    @Test
    void nullLists_areTolerated() {
        ExceptionLogSelector selector = new ExceptionLogSelector(null, null);

        assertThat(selector.isBlacklisted("scope", "app")).isFalse();
    }
}
