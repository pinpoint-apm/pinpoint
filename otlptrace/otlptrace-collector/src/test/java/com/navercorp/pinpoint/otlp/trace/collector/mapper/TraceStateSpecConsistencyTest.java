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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the wire contract of the {@code pp} tracestate entry.
 *
 * <p>The sender is the standalone OpenTelemetry agent extension
 * (<a href="https://github.com/pinpoint-apm/pinpoint-otel-extension">pinpoint-otel-extension</a>).
 * Both repositories hold a byte-identical copy of {@code pp-tracestate-contract.txt}: the extension
 * asserts that it writes these values, this test asserts that {@link PinpointTraceStateParser} reads
 * them back. A change to the format has to update the file in both places and bump its
 * {@code contract-version} line.
 */
class TraceStateSpecConsistencyTest {

    private static final String CONTRACT_FILE = "/pp-tracestate-contract.txt";
    private static final int CONTRACT_VERSION = 1;

    @Test
    void wireFormatConstants_areStable() {
        assertThat(OtlpTraceConstants.TRACESTATE_KEY_PINPOINT).isEqualTo("pp");
        assertThat(OtlpTraceConstants.TRACESTATE_SUBKEY_PARENT_SERVICE_NAME).isEqualTo("svc");
        assertThat(OtlpTraceConstants.TRACESTATE_SUBKEY_PARENT_APPLICATION_NAME).isEqualTo("app");
        assertThat(OtlpTraceConstants.TRACESTATE_SUBKEY_PARENT_APPLICATION_TYPE).isEqualTo("type");
    }

    @Test
    void contractVersion_isPinned() throws IOException {
        List<String> lines = readContract();
        assertThat(lines).isNotEmpty();
        assertThat(lines.get(0)).isEqualTo("# contract-version: " + CONTRACT_VERSION);
    }

    @Test
    void parsesEveryContractValue() throws IOException {
        int parsed = 0;
        int absent = 0;
        for (String line : readContract()) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] cols = line.split("\\|", -1);
            assertThat(cols).as("columns in '%s'", line).hasSize(4);
            String svc = presentValue(cols[0]);
            String app = presentValue(cols[1]);
            Integer type = cols[2].equals("-") ? null : Integer.valueOf(cols[2]);
            String ppValue = cols[3];

            if (ppValue.equals("<null>")) {
                // The sender writes no entry for this input, so there is nothing to parse.
                absent++;
                continue;
            }

            PinpointTraceStateParser.PinpointHeader header =
                    PinpointTraceStateParser.parse(OtlpTraceConstants.TRACESTATE_KEY_PINPOINT + "=" + ppValue);
            assertThat(header).as("header for '%s'", line).isNotNull();
            assertThat(header.parentServiceName()).as("svc in '%s'", line).isEqualTo(svc);
            assertThat(header.parentApplicationName()).as("app in '%s'", line).isEqualTo(app);
            if (svc == null && app == null) {
                // type alone is never written by the sender (see the <null> rows), so a parsed
                // type without svc/app cannot come from this contract.
                assertThat(header.parentApplicationType()).as("type in '%s'", line).isNull();
            } else {
                assertThat(header.parentApplicationType()).as("type in '%s'", line).isEqualTo(type);
            }
            parsed++;
        }
        assertThat(parsed).isGreaterThanOrEqualTo(8);
        assertThat(absent).isGreaterThanOrEqualTo(3);
    }

    @Test
    void contractValue_survivesOtherVendorEntries() throws IOException {
        // The pp entry is one list member among others on the wire.
        PinpointTraceStateParser.PinpointHeader header = PinpointTraceStateParser.parse(
                "dd=s:1;t.dm:-4,pp=svc:order-team;app:order-api;type:1010,nr=opaque");
        assertThat(header).isNotNull();
        assertThat(header.parentServiceName()).isEqualTo("order-team");
        assertThat(header.parentApplicationName()).isEqualTo("order-api");
        assertThat(header.parentApplicationType()).isEqualTo(1010);
    }

    /**
     * A '-' column is an absent input; an empty column is a present but empty input. The sender
     * writes neither, so the parser must return null for both.
     */
    private static String presentValue(String raw) {
        if (raw.equals("-") || raw.isEmpty()) {
            return null;
        }
        return raw;
    }

    private static List<String> readContract() throws IOException {
        InputStream in = TraceStateSpecConsistencyTest.class.getResourceAsStream(CONTRACT_FILE);
        assertThat(in).as("contract file %s", CONTRACT_FILE).isNotNull();
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
