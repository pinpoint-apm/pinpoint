/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.context.grpc.mapper;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.grpc.trace.PException;
import com.navercorp.pinpoint.grpc.trace.PExceptionMetaData;
import com.navercorp.pinpoint.grpc.trace.PStackTraceElement;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionMetaData;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapper;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author intr3p1d
 */
@Mapper(
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        uses = {
                TraceIdMapStructUtils.class,
        }
)
public interface ExceptionMetaDataMapper {

    String EMPTY_STRING = "";

    PExceptionMetaData.Builder pExceptionMetaDataBuilder = PExceptionMetaData.newBuilder();
    PException.Builder pExceptionBuilder = PException.newBuilder();
    PStackTraceElement.Builder pStackTraceElementBuilder = PStackTraceElement.newBuilder();

    default PExceptionMetaData.Builder getpExceptionMetaDataBuilder() {
        PExceptionMetaData.Builder builder = pExceptionMetaDataBuilder;
        builder.clear();
        return builder;
    }

    default PException.Builder getpExceptionBuilder() {
        PException.Builder builder = pExceptionBuilder;
        builder.clear();
        return builder;
    }

    default PStackTraceElement.Builder getpStackTraceElementBuilder() {
        PStackTraceElement.Builder builder = pStackTraceElementBuilder;
        builder.clear();
        return builder;
    }

    @Mappings({
            @Mapping(source = "exceptionWrappers", target = "exceptionsList"),
            @Mapping(source = "traceRoot.traceId", target = "transactionId", qualifiedBy = TraceIdMapStructUtils.ToTransactionId.class),
            @Mapping(source = "traceRoot.traceId.spanId", target = "spanId"),
            @Mapping(source = "traceRoot.shared.uriTemplate", target = "uriTemplate")
    })
    PExceptionMetaData toProto(ExceptionMetaData model);

    default PException toProto(ExceptionWrapper model) {
        if (model == null) {
            return null;
        }

        PException.Builder pException = PException.newBuilder();

        if (model.getStackTraceElements() != null) {
            pException.addAllStackTraceElement(toStackTraceElements(model.getStackTraceElements()));
        }
        if (model.getExceptionClassName() != null) {
            pException.setExceptionClassName(model.getExceptionClassName());
        }
        if (model.getExceptionMessage() != null) {
            pException.setExceptionMessage(model.getExceptionMessage());
        }
        pException.setStartTime(model.getStartTime());
        pException.setExceptionId(model.getExceptionId());
        pException.setExceptionDepth(model.getExceptionDepth());

        return pException.build();
    }

    @Named("toStackTraceElements")
    default List<PStackTraceElement> toStackTraceElements(StackTraceElement[] value) {
        if (ArrayUtils.hasLength(value)) {
            final List<PStackTraceElement> pStackTraceElements = new ArrayList<>();
            for (StackTraceElement stackTraceElement : value) {
                pStackTraceElements.add(toProto(stackTraceElement));
            }
            return pStackTraceElements;
        }
        return Collections.emptyList();
    }

    @Mappings({
            @Mapping(source = "className", target = "className", defaultValue = EMPTY_STRING),
            @Mapping(source = "fileName", target = "fileName", defaultValue = EMPTY_STRING),
            @Mapping(source = "lineNumber", target = "lineNumber", defaultValue = EMPTY_STRING),
            @Mapping(source = "methodName", target = "methodName", defaultValue = EMPTY_STRING),
    })
    PStackTraceElement toProto(StackTraceElement model);
}
