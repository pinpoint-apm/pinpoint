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

import com.navercorp.pinpoint.common.server.bo.exception.StackTraceElementWrapperBo;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class ExceptionFieldLimitsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final Counter counter = registry.counter("test.truncated");
    private final Counter lineCounter = registry.counter("test.truncated.line");

    @Test
    void cap_truncatesOnUtf8Bytes_andCountsOnlyWhenChanged() {
        assertThat(ExceptionFieldLimits.cap("abc", 3, counter)).isEqualTo("abc");
        assertThat(counter.count()).isZero();

        assertThat(ExceptionFieldLimits.cap("abcd", 3, counter)).isEqualTo("abc");
        assertThat(counter.count()).isEqualTo(1.0);

        // 3-byte characters: a 4-byte cap keeps exactly one of them, never a split code point.
        String korean = "가나";
        assertThat(ExceptionFieldLimits.cap(korean, 4, counter)).isEqualTo("가");
        // A cap below the first code point keeps the original rather than emitting "".
        assertThat(ExceptionFieldLimits.cap(korean, 2, counter)).isEqualTo(korean);
    }

    @Test
    void sanitizeUriTemplate_dropsQueryAndFragment_stripsControlChars_caps() {
        assertThat(ExceptionFieldLimits.sanitizeUriTemplate("/users/{id}", 1024, counter)).isEqualTo("/users/{id}");
        assertThat(counter.count()).isZero();

        assertThat(ExceptionFieldLimits.sanitizeUriTemplate("/login?token=SECRET&x=1", 1024, counter)).isEqualTo("/login");
        assertThat(ExceptionFieldLimits.sanitizeUriTemplate("/docs#section?x", 1024, counter)).isEqualTo("/docs");
        assertThat(ExceptionFieldLimits.sanitizeUriTemplate("/a\r\nb/c", 1024, counter)).isEqualTo("/ab/c");
        assertThat(ExceptionFieldLimits.sanitizeUriTemplate("/" + "x".repeat(100), 8, counter)).isEqualTo("/xxxxxxx");
        assertThat(counter.count()).isEqualTo(4.0);

        assertThat(ExceptionFieldLimits.sanitizeUriTemplate(null, 1024, counter)).isEmpty();
        assertThat(ExceptionFieldLimits.sanitizeUriTemplate("?only=query", 1024, counter)).isEmpty();
    }

    @Test
    void boundStackTrace_leavesNormalStackUntouched() {
        String stack = "java.lang.IllegalStateException: boom\n\tat com.example.App.run(App.java:10)\n\tat com.example.Main.main(Main.java:5)\n";

        String bounded = ExceptionFieldLimits.boundStackTrace(stack, 262144, 4096, counter, lineCounter);

        assertThat(bounded).isSameAs(stack);
        assertThat(counter.count()).isZero();
        assertThat(lineCounter.count()).isZero();
    }

    @Test
    void boundStackTrace_cutsWholeTextAtTheLastLineBreak() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("\tat com.example.Deep.frame").append(i).append("(Deep.java:").append(i).append(")\n");
        }
        String stack = sb.toString();

        String bounded = ExceptionFieldLimits.boundStackTrace(stack, 2000, 4096, counter, lineCounter);

        assertThat(bounded.length()).isLessThanOrEqualTo(2000);
        assertThat(bounded).endsWith(")"); // no half frame
        assertThat(bounded.chars().filter(c -> c == '\n').count()).isGreaterThan(30);
        assertThat(counter.count()).isEqualTo(1.0);
        assertThat(lineCounter.count()).isZero();
    }

    @Test
    void boundStackTrace_cutsOnlyTheLongLines_keepsTheOthers() {
        String longLine = "at " + "x (".repeat(5000);
        String stack = "\tat com.example.App.run(App.java:10)\n" + longLine + "\n\tat com.example.Main.main(Main.java:5)";

        String bounded = ExceptionFieldLimits.boundStackTrace(stack, 0, 100, counter, lineCounter);

        String[] lines = bounded.split("\n");
        assertThat(lines).hasSize(3);
        assertThat(lines[0]).isEqualTo("\tat com.example.App.run(App.java:10)");
        assertThat(lines[1]).hasSize(100).isEqualTo(longLine.substring(0, 100));
        assertThat(lines[2]).isEqualTo("\tat com.example.Main.main(Main.java:5)");
        assertThat(lineCounter.count()).isEqualTo(1.0);
        assertThat(counter.count()).isZero();
    }

    @Test
    void boundStackTrace_unlimitedWhenBoundsAreNonPositive() {
        String longLine = "at " + "x (".repeat(5000);

        assertThat(ExceptionFieldLimits.boundStackTrace(longLine, 0, 0, counter, lineCounter)).isSameAs(longLine);
        assertThat(ExceptionFieldLimits.boundStackTrace("", 10, 10, counter, lineCounter)).isEmpty();
        assertThat(ExceptionFieldLimits.boundStackTrace(null, 10, 10, counter, lineCounter)).isNull();
    }

    /**
     * The regression this whole class exists for: the Node/.NET frame regexes are quadratic on a
     * long line without a closing bracket (measured ~1.4s at 48KB, x4 per doubling). With the line
     * cap the parser sees at most 4KB per line, so even a multi-megabyte hostile line parses in
     * milliseconds — on both the span-event and the log-record path, which share this parser.
     */
    @Test
    void parseStackTrace_hostileSingleLine_isBoundedInTime() {
        OtlpExceptionMapper mapper = new OtlpExceptionMapper(2048, 256, 2048, registry);
        // 1.5MB single line: "at " followed by many candidate "(...)" starts and no terminal ")".
        String hostile = "at " + "x (".repeat(500_000);
        assertThat(hostile.getBytes(StandardCharsets.UTF_8).length).isGreaterThan(1_000_000);

        List<StackTraceElementWrapperBo> nodeFrames = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> mapper.parseStackTrace(hostile, "nodejs"));
        List<StackTraceElementWrapperBo> dotnetFrames = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> mapper.parseStackTrace(hostile, "dotnet"));
        List<StackTraceElementWrapperBo> sniffed = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> mapper.parseStackTrace(hostile, null));

        // Whatever the parsers make of the truncated line, the input was bounded and counted.
        assertThat(nodeFrames.size() + dotnetFrames.size() + sniffed.size()).isLessThanOrEqualTo(3);
        assertThat(registry.get("collector.otlptrace.exception.truncated").tag("field", "stacktrace_line").counter().count()).isEqualTo(3.0);
        assertThat(registry.get("collector.otlptrace.exception.truncated").tag("field", "stacktrace_bytes").counter().count()).isEqualTo(3.0);
    }
}
