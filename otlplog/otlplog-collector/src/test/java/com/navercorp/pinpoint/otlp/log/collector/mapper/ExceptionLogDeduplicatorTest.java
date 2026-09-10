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

import com.navercorp.pinpoint.otlp.trace.collector.mapper.ExceptionAttributeUtils;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.IdAndName;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperUtils;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import io.opentelemetry.proto.logs.v1.LogRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.OTHER_SPAN_ID;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.SPAN_ID;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.exceptionRecord;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.kv;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.kvInt;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.record;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionLogDeduplicatorTest {

    private static final IdAndName ID = new IdAndName("agent-1", "agent-1", "app-1", "default");
    private static final String TYPE = "java.lang.IllegalStateException";
    private static final String STACK = "java.lang.IllegalStateException: boom\n\tat com.example.App.run(App.java:10)\n";

    private static ExceptionLogCandidate candidate(LogRecord record) {
        return new ExceptionLogCandidate(ID, "java", record, false);
    }

    @Test
    void firstRecordWins_laterSameKeyIsDuplicate() {
        ExceptionLogDeduplicator dedup = new ExceptionLogDeduplicator();
        LogRecord instrumentation = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build();
        LogRecord appender = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).setBody(kv("b", "x").getValue()).build();

        assertThat(dedup.offer(candidate(instrumentation))).isTrue();
        assertThat(dedup.offer(candidate(appender))).isFalse();

        assertThat(dedup.winners()).extracting(ExceptionLogCandidate::record).containsExactly(instrumentation);
    }

    @Test
    void recordWithLongerStackTrace_takesOver_butStillCountsAsDuplicate() {
        // Appender record (type + message only) arrives first, the instrumentation record with the
        // full stack second: the stack must survive, and the collision is counted once.
        ExceptionLogDeduplicator dedup = new ExceptionLogDeduplicator();
        LogRecord appender = exceptionRecord(SPAN_ID, TYPE, "boom", null).build();
        LogRecord instrumentation = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build();

        assertThat(dedup.offer(candidate(appender))).isTrue();
        assertThat(dedup.offer(candidate(instrumentation))).isFalse();

        assertThat(dedup.winners()).extracting(ExceptionLogCandidate::record).containsExactly(instrumentation);
    }

    @Test
    void differentSpanOrType_areDistinctKeys() {
        ExceptionLogDeduplicator dedup = new ExceptionLogDeduplicator();
        LogRecord a = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build();
        LogRecord otherSpan = exceptionRecord(OTHER_SPAN_ID, TYPE, "boom", STACK).build();
        LogRecord otherType = exceptionRecord(SPAN_ID, "java.io.IOException", "boom", STACK).build();

        assertThat(dedup.offer(candidate(a))).isTrue();
        assertThat(dedup.offer(candidate(otherSpan))).isTrue();
        assertThat(dedup.offer(candidate(otherType))).isTrue();

        assertThat(dedup.winners()).hasSize(3);
    }

    @Test
    void winnersKeepFirstSeenOrder() {
        ExceptionLogDeduplicator dedup = new ExceptionLogDeduplicator();
        LogRecord first = exceptionRecord(SPAN_ID, "A", "a", null).build();
        LogRecord second = exceptionRecord(SPAN_ID, "B", "b", null).build();
        LogRecord firstAgainLongerStack = exceptionRecord(SPAN_ID, "A", "a", STACK).build();

        dedup.offer(candidate(first));
        dedup.offer(candidate(second));
        dedup.offer(candidate(firstAgainLongerStack));

        assertThat(List.copyOf(dedup.winners())).extracting(ExceptionLogCandidate::record)
                .containsExactly(firstAgainLongerStack, second);
    }

    @Test
    void exceptionType_fallsBackToErrorType_thenNull_withTheMapperSemantics() {
        assertThat(ExceptionLogDeduplicator.exceptionType(record(SPAN_ID, kv("exception.type", "T"), kv("error.type", "E")).build())).isEqualTo("T");
        assertThat(ExceptionLogDeduplicator.exceptionType(record(SPAN_ID, kv("error.type", "E"), kv("exception.message", "m")).build())).isEqualTo("E");
        assertThat(ExceptionLogDeduplicator.exceptionType(record(SPAN_ID, kv("exception.message", "m")).build())).isNull();
        // An empty or non-string exception.type counts as absent, exactly as the mapper resolves it.
        assertThat(ExceptionLogDeduplicator.exceptionType(record(SPAN_ID, kv("exception.type", ""), kv("error.type", "E")).build())).isEqualTo("E");
        assertThat(ExceptionLogDeduplicator.exceptionType(record(SPAN_ID, kvInt("exception.type", 7), kv("error.type", "E")).build())).isEqualTo("E");
        assertThat(ExceptionLogDeduplicator.exceptionType(record(SPAN_ID, kv("exception.type", ""), kv("error.type", "")).build())).isNull();
    }

    @Test
    void exceptionType_agreesWithTheMapperResolver() {
        LogRecord[] records = {
                record(SPAN_ID, kv("exception.type", "T"), kv("error.type", "E")).build(),
                record(SPAN_ID, kv("exception.type", ""), kv("error.type", "E")).build(),
                record(SPAN_ID, kvInt("exception.type", 7), kv("error.type", "E")).build(),
                record(SPAN_ID, kv("error.type", "E")).build(),
                record(SPAN_ID, kv("exception.message", "m")).build(),
                record(SPAN_ID, kv("exception.type", ""), kv("error.type", "")).build(),
        };
        for (LogRecord record : records) {
            Map<String, AttributeValue> attributes = OtlpTraceMapperUtils.getAttributeValueMap(record.getAttributesList());
            // The mapper treats an empty resolved type as "no type" (Optional.empty), which the
            // deduplicator expresses as null.
            String resolved = ExceptionAttributeUtils.resolveExceptionType(attributes, attributes);
            String mapperEffectiveType = StringUtils.hasLength(resolved) ? resolved : null;
            assertThat(ExceptionLogDeduplicator.exceptionType(record))
                    .as("record %s", record.getAttributesList())
                    .isEqualTo(mapperEffectiveType);
        }
    }

    @Test
    void emptyExceptionType_isKeyedByTheErrorTypeFallback_likeTheMapperStoresIt() {
        // Both records end up stored under "E"; the second must be the duplicate.
        ExceptionLogDeduplicator dedup = new ExceptionLogDeduplicator();
        LogRecord byErrorType = record(SPAN_ID, kv("exception.type", ""), kv("error.type", "E"), kv("exception.message", "m")).build();
        LogRecord byExceptionType = record(SPAN_ID, kv("exception.type", "E"), kv("exception.message", "m")).build();

        assertThat(dedup.offer(candidate(byErrorType))).isTrue();
        assertThat(dedup.offer(candidate(byExceptionType))).isFalse();
        assertThat(dedup.winners()).hasSize(1);
    }

    @Test
    void recordsWithoutAResolvedType_areNeverDeduplicated() {
        // The mapper rejects each of these as no_exception_type; folding them into one key would
        // report one rejection for three dropped records.
        ExceptionLogDeduplicator dedup = new ExceptionLogDeduplicator();
        LogRecord messageOnly = record(SPAN_ID, kv("exception.message", "a")).build();
        LogRecord messageOnlyAgain = record(SPAN_ID, kv("exception.message", "b")).build();
        LogRecord emptyTypes = record(SPAN_ID, kv("exception.type", ""), kv("error.type", "")).build();
        LogRecord typed = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build();

        assertThat(dedup.offer(candidate(messageOnly))).isTrue();
        assertThat(dedup.offer(candidate(messageOnlyAgain))).isTrue();
        assertThat(dedup.offer(candidate(emptyTypes))).isTrue();
        assertThat(dedup.offer(candidate(typed))).isTrue();
        assertThat(dedup.offer(candidate(typed))).isFalse();

        assertThat(List.copyOf(dedup.winners())).extracting(ExceptionLogCandidate::record)
                .containsExactly(messageOnly, messageOnlyAgain, emptyTypes, typed);
    }

    @Test
    void stackTraceLength_zeroWhenAbsent() {
        assertThat(ExceptionLogDeduplicator.stackTraceLength(exceptionRecord(SPAN_ID, TYPE, "m", null).build())).isZero();
        assertThat(ExceptionLogDeduplicator.stackTraceLength(exceptionRecord(SPAN_ID, TYPE, "m", STACK).build())).isEqualTo(STACK.length());
    }
}
