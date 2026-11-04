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
package com.navercorp.pinpoint.web.trace.view;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import com.navercorp.pinpoint.web.trace.callstacks.ErrorKey;

public class TransactionCallTreeCallStackSerializer extends ValueSerializer<TransactionCallTreeViewModel.CallStack> {

    @Override
    public void serialize(TransactionCallTreeViewModel.CallStack value, JsonGenerator jgen, SerializationContext provider) {
        jgen.writeStartArray();
        jgen.writeNumber(value.getBegin());
        jgen.writeNumber(value.getEnd());
        jgen.writeBoolean(value.isExcludeFromTimeline());
        jgen.writeString(value.getApplicationName());
        jgen.writeNumber(value.getTab());
        jgen.writeNumber(value.getId());
        writeInteger(jgen, value.getParentId());
        jgen.writeBoolean(value.isMethod());
        jgen.writeBoolean(value.isHasChild());
        jgen.writeString(value.getTitle());
        // index 10
        jgen.writeString(value.getArguments());
        jgen.writeString(value.getExecuteTime());
        writeLong(jgen, value.getGap());
        writeLong(jgen, value.getElapsedTime());
        writeInteger(jgen, value.getBarWidth());
        writeLong(jgen, value.getExecutionMilliseconds());
        jgen.writeString(value.getSimpleClassName());
        jgen.writeNumber(value.getMethodType());
        jgen.writeString(value.getApiType());
        jgen.writeString(value.getAgent());
        // index 20
        jgen.writeBoolean(value.isHasException());
        jgen.writeBoolean(value.isAuthorized());
        jgen.writeString(value.getAgentName());
        jgen.writeNumber(value.getLineNumber());
        jgen.writeString(value.getLocation());
        jgen.writeString(value.getApplicationServiceType());
        writeErrorKey(jgen, value.getErrorKey());
        jgen.writeString(value.getServiceName());
        writeLong(jgen, value.getGapNanos());
        writeLong(jgen, value.getElapsedTimeNanos());
        writeLong(jgen, value.getExecutionNanos());
        writeLong(jgen, value.getBeginOffsetNanos());
        writeLong(jgen, value.getEndOffsetNanos());
        jgen.writeEndArray();
    }

    void writeLong(JsonGenerator jgen, Long value) {
        if (value == null) {
            jgen.writeNull();
        } else {
            jgen.writeNumber(value);
        }
    }

    void writeInteger(JsonGenerator jgen, Integer value) {
        if (value == null) {
            jgen.writeNull();
        } else {
            jgen.writeNumber(value);
        }
    }

    void writeErrorKey(JsonGenerator jgen, ErrorKey errorKey) {
        if (errorKey == null) {
            jgen.writeNull();
            return;
        }
        jgen.writeStartObject();
        jgen.writeStringProperty("serviceName", errorKey.serviceName());
        jgen.writeStringProperty("applicationName", errorKey.applicationName());
        jgen.writeStringProperty("agentId", errorKey.agentId());
        jgen.writeStringProperty("transactionId", errorKey.transactionId());
        // json number overflow
        jgen.writeStringProperty("spanId", String.valueOf(errorKey.spanId()));
        jgen.writeStringProperty("exceptionId", String.valueOf(errorKey.exceptionId()));
        jgen.writeEndObject();
    }
}
