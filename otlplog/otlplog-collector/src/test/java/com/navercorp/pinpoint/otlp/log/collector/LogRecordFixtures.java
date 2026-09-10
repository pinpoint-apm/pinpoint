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

package com.navercorp.pinpoint.otlp.log.collector;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import io.opentelemetry.proto.resource.v1.Resource;

/** Builders for the OTLP logs shapes the tests exercise (Java agent / appender style records). */
public final class LogRecordFixtures {

    public static final byte[] TRACE_ID = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    public static final byte[] SPAN_ID = {1, 2, 3, 4, 5, 6, 7, 8};
    public static final byte[] OTHER_SPAN_ID = {9, 9, 9, 9, 9, 9, 9, 9};

    public static final int SAMPLED = 0x01;

    public static final String APPLICATION_NAME = "app-1";
    public static final String AGENT_ID = "agent-1";

    private LogRecordFixtures() {
    }

    public static KeyValue kv(String key, String value) {
        return KeyValue.newBuilder().setKey(key).setValue(AnyValue.newBuilder().setStringValue(value)).build();
    }

    public static KeyValue kvInt(String key, long value) {
        return KeyValue.newBuilder().setKey(key).setValue(AnyValue.newBuilder().setIntValue(value)).build();
    }

    /** A record inside a sampled span with the given attributes; no timestamp unless set by the caller. */
    public static LogRecord.Builder record(byte[] spanId, KeyValue... attributes) {
        LogRecord.Builder builder = LogRecord.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID))
                .setSpanId(ByteString.copyFrom(spanId))
                .setFlags(SAMPLED);
        for (KeyValue attribute : attributes) {
            builder.addAttributes(attribute);
        }
        return builder;
    }

    /** The shape the Java agent emits for a logged exception. */
    public static LogRecord.Builder exceptionRecord(byte[] spanId, String type, String message, String stacktrace) {
        LogRecord.Builder builder = record(spanId, kv("exception.type", type), kv("exception.message", message));
        if (stacktrace != null) {
            builder.addAttributes(kv("exception.stacktrace", stacktrace));
        }
        return builder;
    }

    /** A log line with no exception attributes. */
    public static LogRecord.Builder plainRecord(byte[] spanId, String body) {
        return record(spanId, kv("thread.name", "main"))
                .setBody(AnyValue.newBuilder().setStringValue(body));
    }

    public static Resource resource(KeyValue... attributes) {
        Resource.Builder builder = Resource.newBuilder();
        for (KeyValue attribute : attributes) {
            builder.addAttributes(attribute);
        }
        return builder.build();
    }

    /** A resource that resolves to (app-1, agent-1) without the applicationName fallback. */
    public static Resource validResource() {
        return resource(kv("pinpoint.applicationName", APPLICATION_NAME), kv("pinpoint.agentId", AGENT_ID),
                kv("telemetry.sdk.language", "java"));
    }

    public static ResourceLogs resourceLogs(Resource resource, String scopeName, LogRecord... records) {
        ScopeLogs.Builder scope = ScopeLogs.newBuilder()
                .setScope(InstrumentationScope.newBuilder().setName(scopeName));
        for (LogRecord record : records) {
            scope.addLogRecords(record);
        }
        return ResourceLogs.newBuilder().setResource(resource).addScopeLogs(scope).build();
    }

    public static ResourceLogs resourceLogs(LogRecord... records) {
        return resourceLogs(validResource(), "com.example.App", records);
    }
}
