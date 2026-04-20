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


import com.navercorp.pinpoint.grpc.trace.*;
import com.navercorp.pinpoint.io.SpanVersion;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueType;
import com.navercorp.pinpoint.common.trace.attribute.AttributeKeyValue;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.AsyncSpanChunk;
import com.navercorp.pinpoint.profiler.context.LocalAsyncId;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanUriGetter;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author intr3p1d
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.JSR330,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {
                TraceIdMapStructUtils.class,
                AnnotationValueMapper.class,
                SpanUriGetter.class,
                MapperUtils.class,
        }
)
public interface SpanMessageMapper {
    String DEFAULT_REMOTE_ADDRESS = "UNKNOWN";
    String DEFAULT_END_POINT = "UNKNOWN";

    AnnotationValueMapper ANNOTATION_VALUE_MAPPER = Mappers.getMapper(AnnotationValueMapper.class);

    @Mapping(source = "applicationServiceType", target = "version", qualifiedByName = "spanVersion")
    @Mapping(source = "applicationServiceType", target = "applicationServiceType")
    @Mapping(source = "span.traceRoot.traceId", target = "transactionId", qualifiedBy = TraceIdMapStructUtils.ToTransactionId.class)
    @Mapping(source = "span.traceRoot.traceId.spanId", target = "spanId")
    @Mapping(source = "span.traceRoot.traceId.parentSpanId", target = "parentSpanId")
    @Mapping(source = "span.elapsedTime", target = "elapsed")
    @Mapping(source = "span", target = "acceptEvent", qualifiedByName = "toAcceptEvent")
    @Mapping(source = "span.traceRoot.traceId.flags", target = "flag")
    @Mapping(source = "span.traceRoot.shared.errorCode", target = "err")
    @Mapping(source = "span.exceptionInfo", target = "exceptionInfo")
    @Mapping(source = "span.traceRoot.shared.loggingInfo", target = "loggingTransactionInfo")
    @Mapping(source = "span.annotations", target = "annotation")
    @Mapping(source = "span.attributes", target = "attribute")
    @Mapping(source = "span.spanEventList", target = "spanEvent")
    void map(Span span, short applicationServiceType, @MappingTarget PSpan.Builder builder);

    default void map(SpanChunk spanChunk, short applicationServiceType, @MappingTarget PSpanChunk.Builder builder) {
        if (spanChunk instanceof AsyncSpanChunk) {
            toAsyncSpanChunk((AsyncSpanChunk) spanChunk, applicationServiceType, builder);
        } else {
            toPSpanChunk(spanChunk, applicationServiceType, builder);
        }
    }

    @Mapping(source = "applicationServiceType", target = "version", qualifiedByName = "spanVersion")
    @Mapping(source = "applicationServiceType", target = "applicationServiceType")
    @Mapping(source = "spanChunk.traceRoot.traceId", target = "transactionId", qualifiedBy = TraceIdMapStructUtils.ToTransactionId.class)
    @Mapping(source = "spanChunk.traceRoot.traceId.spanId", target = "spanId")
    @Mapping(source = "spanChunk.traceRoot.shared.endPoint", target = "endPoint")
    @Mapping(source = "spanChunk.spanEventList", target = "spanEvent")
    @Mapping(target = "keyTime", ignore = true)
    @Mapping(target = "localAsyncId", ignore = true)
    void toPSpanChunk(SpanChunk spanChunk, short applicationServiceType, @MappingTarget PSpanChunk.Builder builder);

    @InheritConfiguration
    @Mapping(source = "asyncSpanChunk.localAsyncId", target = "localAsyncId")
    void toAsyncSpanChunk(AsyncSpanChunk asyncSpanChunk, short applicationServiceType, @MappingTarget PSpanChunk.Builder builder);

    PLocalAsyncId map(LocalAsyncId localAsyncId);


    @Named("spanVersion")
    default int spanVersion(short applicationServiceType) {
        return SpanVersion.TRACE_V2;
    }


    @Mapping(source = "elapsedTime", target = "endElapsed")
    @Mapping(source = "depth", target = "depth", conditionQualifiedBy = MapperUtils.IsNotMinusOne.class)
    @Mapping(source = ".", target = "nextEvent")
    @Mapping(source = "asyncIdObject.asyncId", target = "asyncEvent")
    @Mapping(source = "annotations", target = "annotation")
    @Mapping(source = "attributes", target = "attribute")
    @Mapping(target = "startElapsed", ignore = true)
    PSpanEvent map(SpanEvent spanEvent);

    @Mapping(source = ".", target = "messageEvent")
    PNextEvent mapNextEvent(SpanEvent spanEvent);

    @Mapping(source = "nextSpanId", target = "nextSpanId", conditionQualifiedBy = MapperUtils.IsNotMinusOne.class)
    PMessageEvent mapMessageEvent(SpanEvent spanEvent);

    default PAnnotation map(Annotation annotation) {
        if(annotation == null) {
            return null;
        }
        PAnnotation.Builder builder = PAnnotation.newBuilder();
        PAnnotationValue value = ANNOTATION_VALUE_MAPPER.map(annotation);
        if(value != null) {
            builder.setValue(value);
        }
        builder.setKey(annotation.getKey());
        return builder.build();
    }

    default PAttribute map(AttributeKeyValue attribute) {
        if (attribute == null) {
            return null;
        }
        PAttribute.Builder builder = PAttribute.newBuilder();
        builder.setKey(attribute.getKey());
        AttributeValue value = attribute.getValue();
        if (value != null) {
            builder.setValue(mapAttributeValue(value));
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    default PAttributeValue mapAttributeValue(AttributeValue attributeValue) {
        PAttributeValue.Builder valueBuilder = PAttributeValue.newBuilder();
        AttributeValueType type = attributeValue.getType();
        switch (type) {
            case STRING:
                valueBuilder.setStringValue((String) attributeValue.getValue());
                break;
            case BOOLEAN:
                valueBuilder.setBoolValue((Boolean) attributeValue.getValue());
                break;
            case LONG:
                valueBuilder.setLongValue((Long) attributeValue.getValue());
                break;
            case DOUBLE:
                valueBuilder.setDoubleValue((Double) attributeValue.getValue());
                break;
            case BYTES:
                valueBuilder.setBinaryValue(com.google.protobuf.ByteString.copyFrom((byte[]) attributeValue.getValue()));
                break;
            case ARRAY:
                PAttributeArrayValue.Builder arrayBuilder = PAttributeArrayValue.newBuilder();
                for (AttributeValue item : (java.util.List<AttributeValue>) attributeValue.getValue()) {
                    if (item != null) {
                        arrayBuilder.addValues(mapAttributeValue(item));
                    }
                }
                valueBuilder.setArrayValue(arrayBuilder.build());
                break;
            case KEY_VALUE_LIST:
                PAttributeKeyValueList.Builder kvBuilder = PAttributeKeyValueList.newBuilder();
                for (AttributeKeyValue kv : (java.util.List<AttributeKeyValue>) attributeValue.getValue()) {
                    PAttribute.Builder attrBuilder = PAttribute.newBuilder();
                    attrBuilder.setKey(kv.getKey());
                    if (kv.getValue() != null) {
                        attrBuilder.setValue(mapAttributeValue(kv.getValue()));
                    }
                    kvBuilder.addValues(attrBuilder.build());
                }
                valueBuilder.setKvlistValue(kvBuilder.build());
                break;
        }
        return valueBuilder.build();
    }

    @Named("toAcceptEvent")
    @Mapping(source = "remoteAddr", target = "remoteAddr", defaultValue = DEFAULT_REMOTE_ADDRESS)
    @Mapping(source = "traceRoot.shared", target = "rpc", qualifiedBy = SpanUriGetter.ToCollectedUri.class)
    @Mapping(source = "traceRoot.shared.endPoint", target = "endPoint", defaultValue = DEFAULT_END_POINT)
    @Mapping(source = ".", target = "parentInfo")
    PAcceptEvent toAcceptEvent(Span span);

    @Mapping(source = "parentApplicationType", target = "parentApplicationType", conditionQualifiedBy = MapperUtils.IsNotZeroShort.class)
    PParentInfo toParentInfo(Span span);
}
