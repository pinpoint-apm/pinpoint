/*
 * Copyright 2015 NAVER Corp.
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
package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author jaehong.kim
 */
public class TransactionInfoCallStackSerializer extends JsonSerializer<TransactionInfoViewModel.CallStack> {

    @Override
    public void serialize(TransactionInfoViewModel.CallStack value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeStartArray();
        jgen.writeString(value.getDepth());
        jgen.writeNumber(value.getBegin());
        jgen.writeNumber(value.getEnd());
        jgen.writeBoolean(value.isExcludeFromTimeline());
        jgen.writeString(value.getApplicationName());
        jgen.writeNumber(value.getTab());
        jgen.writeString(value.getId());
        jgen.writeString(value.getParentId());
        jgen.writeBoolean(value.isMethod());
        jgen.writeBoolean(value.isHasChild());
        jgen.writeString(value.getTitle());
        jgen.writeString(value.getArguments());
        jgen.writeString(value.getExecuteTime());
        jgen.writeString(value.getGap());
        jgen.writeString(value.getElapsedTime());
        jgen.writeString(value.getBarWidth());
        jgen.writeString(value.getExecutionMilliseconds());
        jgen.writeString(value.getSimpleClassName());
        jgen.writeString(value.getMethodType());
        jgen.writeString(value.getApiType());
        jgen.writeString(value.getAgent());
        jgen.writeBoolean(value.isFocused());
        jgen.writeBoolean(value.isHasException());
        jgen.writeBoolean(value.isAuthorized());
        jgen.writeEndArray();
    }
}
