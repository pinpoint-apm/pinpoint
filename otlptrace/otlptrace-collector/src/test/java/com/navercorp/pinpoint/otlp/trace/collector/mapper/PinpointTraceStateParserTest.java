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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PinpointTraceStateParserTest {

    @Test
    void parse_null() {
        assertThat(PinpointTraceStateParser.parse(null)).isNull();
    }

    @Test
    void parse_empty() {
        assertThat(PinpointTraceStateParser.parse("")).isNull();
    }

    @Test
    void parse_noPinpointEntry() {
        assertThat(PinpointTraceStateParser.parse("dd=s:1;t.dm:-4,nr=opaque")).isNull();
    }

    @Test
    void parse_bothSubKeys() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=svc:my-svc;app:my-app");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("my-svc");
        assertThat(h.parentApplicationName()).isEqualTo("my-app");
    }

    @Test
    void parse_svcOnly() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=svc:my-svc");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("my-svc");
        assertThat(h.parentApplicationName()).isNull();
    }

    @Test
    void parse_appOnly() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=app:my-app");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isNull();
        assertThat(h.parentApplicationName()).isEqualTo("my-app");
    }

    @Test
    void parse_multipleVendors_extractsPinpoint() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("dd=s:1,pp=svc:my-svc;app:my-app,nr=opaque");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("my-svc");
        assertThat(h.parentApplicationName()).isEqualTo("my-app");
    }

    @Test
    void parse_duplicatePinpoint_firstWins() {
        // W3C tracestate: on duplicate keys, the first list-member wins.
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=svc:first,pp=svc:second");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("first");
    }

    @Test
    void parse_whitespaceAroundSeparators() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse(" pp = svc : my-svc ; app : my-app ");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("my-svc");
        assertThat(h.parentApplicationName()).isEqualTo("my-app");
    }

    @Test
    void parse_unknownSubKey_ignored() {
        // Forward compatibility: unknown sub-keys (e.g. future "type") must not break parsing.
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=svc:my-svc;type:1010;app:my-app");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("my-svc");
        assertThat(h.parentApplicationName()).isEqualTo("my-app");
    }

    @Test
    void parse_emptySubValue_skipped() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=svc:;app:my-app");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isNull();
        assertThat(h.parentApplicationName()).isEqualTo("my-app");
    }

    @Test
    void parse_emptyValue_returnsNull() {
        assertThat(PinpointTraceStateParser.parse("pp=")).isNull();
    }

    @Test
    void parse_malformedSubKey_skipped() {
        // No ':' separator inside sub-entry — skip that fragment, keep parsing the rest.
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=garbage;svc:my-svc");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("my-svc");
        assertThat(h.parentApplicationName()).isNull();
    }

    @Test
    void parse_onlyMalformedSubEntries_returnsNull() {
        assertThat(PinpointTraceStateParser.parse("pp=onlygarbage")).isNull();
    }

    @Test
    void parse_malformedTopLevelEntry_skipped() {
        // top-level entry without '=' — skipped, but a later valid pp= still wins.
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("garbage,pp=svc:my-svc;app:my-app");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("my-svc");
    }

    @Test
    void parse_allThreeSubKeys() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=svc:my-svc;app:my-app;type:1010");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("my-svc");
        assertThat(h.parentApplicationName()).isEqualTo("my-app");
        assertThat(h.parentApplicationType()).isEqualTo(1010);
    }

    @Test
    void parse_typeOnly() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=type:1220");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isNull();
        assertThat(h.parentApplicationName()).isNull();
        assertThat(h.parentApplicationType()).isEqualTo(1220);
    }

    @Test
    void parse_typeMissing_isNull() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=svc:my-svc;app:my-app");
        assertThat(h).isNotNull();
        assertThat(h.parentApplicationType()).isNull();
    }

    @Test
    void parse_typeNegative_acceptedWithinIntRange() {
        // ServiceType.UNDEFINED uses -1; negative codes must be accepted.
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=app:x;type:-1");
        assertThat(h).isNotNull();
        assertThat(h.parentApplicationType()).isEqualTo(-1);
    }

    @Test
    void parse_typeNonNumeric_droppedKeepsOtherFields() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=svc:my-svc;type:tomcat;app:my-app");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("my-svc");
        assertThat(h.parentApplicationName()).isEqualTo("my-app");
        assertThat(h.parentApplicationType()).isNull();
    }

    @Test
    void parse_typeAboveShortRange_acceptedAsInt() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=app:x;type:32768");
        assertThat(h).isNotNull();
        assertThat(h.parentApplicationType()).isEqualTo(32768);
    }

    @Test
    void parse_typeBelowShortRange_acceptedAsInt() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=app:x;type:-32769");
        assertThat(h).isNotNull();
        assertThat(h.parentApplicationType()).isEqualTo(-32769);
    }

    @Test
    void parse_duplicateSubKey_svc_firstWins() {
        // Mirrors the W3C top-level "first list-member wins" rule for sub-keys inside
        // the pp value. A duplicate cannot overwrite the earlier assignment.
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=svc:first;svc:second");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("first");
    }

    @Test
    void parse_duplicateSubKey_app_firstWins() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=app:first;app:second");
        assertThat(h).isNotNull();
        assertThat(h.parentApplicationName()).isEqualTo("first");
    }

    @Test
    void parse_duplicateSubKey_type_firstWins() {
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=type:1010;type:2020");
        assertThat(h).isNotNull();
        assertThat(h.parentApplicationType()).isEqualTo(1010);
    }

    @Test
    void parse_duplicateSubKey_type_firstInvalidThenValid_recovers() {
        // For 'type' specifically, the slot is null when the first occurrence's value
        // fails numeric parsing, so a subsequent well-formed value still populates it.
        // This is "first valid value wins" — slightly looser than strict first-occurrence
        // wins, but more useful when one of the duplicates is plain garbage.
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=type:tomcat;type:1010");
        assertThat(h).isNotNull();
        assertThat(h.parentApplicationType()).isEqualTo(1010);
    }

    @Test
    void parse_duplicateSubKey_mixed_eachFieldIndependentlyFirstWins() {
        // The first-wins rule is per sub-key, not per entire value.
        PinpointTraceStateParser.PinpointHeader h =
                PinpointTraceStateParser.parse("pp=svc:s1;app:a1;svc:s2;app:a2;type:1010;type:2020");
        assertThat(h).isNotNull();
        assertThat(h.parentServiceName()).isEqualTo("s1");
        assertThat(h.parentApplicationName()).isEqualTo("a1");
        assertThat(h.parentApplicationType()).isEqualTo(1010);
    }

    @Test
    void parse_typeAtIntBoundaries_accepted() {
        PinpointTraceStateParser.PinpointHeader max =
                PinpointTraceStateParser.parse("pp=app:x;type:2147483647");
        assertThat(max).isNotNull();
        assertThat(max.parentApplicationType()).isEqualTo(Integer.MAX_VALUE);

        PinpointTraceStateParser.PinpointHeader min =
                PinpointTraceStateParser.parse("pp=app:x;type:-2147483648");
        assertThat(min).isNotNull();
        assertThat(min.parentApplicationType()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    void parse_typeOutOfIntRange_dropped() {
        PinpointTraceStateParser.PinpointHeader max =
                PinpointTraceStateParser.parse("pp=app:x;type:2147483648");
        assertThat(max).isNotNull();
        assertThat(max.parentApplicationType()).isNull();

        PinpointTraceStateParser.PinpointHeader min =
                PinpointTraceStateParser.parse("pp=app:x;type:-2147483649");
        assertThat(min).isNotNull();
        assertThat(min.parentApplicationType()).isNull();
    }
}
