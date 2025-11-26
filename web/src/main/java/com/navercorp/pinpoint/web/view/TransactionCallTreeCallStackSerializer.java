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
package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class TransactionCallTreeCallStackSerializer extends JsonSerializer<TransactionCallTreeViewModel.CallStack> {

    @Override
    public void serialize(TransactionCallTreeViewModel.CallStack value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartArray();
        jgen.writeString(value.getDepth());
        jgen.writeNumber(value.getBegin());
        jgen.writeNumber(value.getEnd());
        jgen.writeBoolean(value.isExcludeFromTimeline());
        jgen.writeString(value.getApplicationName());
        jgen.writeNumber(value.getTab());
        jgen.writeNumber(value.getId());
        writeInteger(jgen, value.getParentId());
        jgen.writeBoolean(value.isMethod());
        jgen.writeBoolean(value.isHasChild());
        // index 10
        jgen.writeString(value.getTitle());
        jgen.writeString(value.getArguments());
        jgen.writeString(value.getExecuteTime());
        writeLong(jgen, value.getGap());
        writeLong(jgen, value.getElapsedTime());
        writeInteger(jgen, value.getBarWidth());
        writeLong(jgen, value.getExecutionMilliseconds());
        jgen.writeString(value.getSimpleClassName());
        jgen.writeNumber(value.getMethodType());
        jgen.writeString(value.getApiType());
        // index 20
        jgen.writeString(value.getAgent());
        jgen.writeBoolean(value.isFocused());
        jgen.writeBoolean(value.isHasException());
        jgen.writeBoolean(value.isAuthorized());
        jgen.writeString(value.getAgentName());
        jgen.writeNumber(value.getLineNumber());
        jgen.writeString(value.getLocation());
        jgen.writeString(value.getApplicationServiceType());
        // json number overflow
        jgen.writeString(String.valueOf(value.getExceptionChainId()));
        jgen.writeEndArray();
    }

    void writeLong(JsonGenerator jgen, Long value) throws IOException {
        if (value == null) {
            jgen.writeNull();
        } else {
            jgen.writeNumber(value);
        }
    }

    void writeInteger(JsonGenerator jgen, Integer value) throws IOException {
        if (value == null) {
            jgen.writeNull();
        } else {
            jgen.writeNumber(value);
        }
    }
}
