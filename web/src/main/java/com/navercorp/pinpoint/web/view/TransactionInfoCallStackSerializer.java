package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created by Naver on 2015-10-22.
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
        jgen.writeBoolean(value.isFocused());
        jgen.writeBoolean(value.isHasException());
        jgen.writeString(value.getLogButtonName());
        jgen.writeString(value.getLogPageUrl());
        jgen.writeEndArray();
    }
}
