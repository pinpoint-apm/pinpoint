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

import com.google.protobuf.ByteString;
import com.google.protobuf.StringValue;
import com.navercorp.pinpoint.common.util.BytesStringStringValue;
import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.grpc.trace.PAnnotationValue;
import com.navercorp.pinpoint.grpc.trace.PBytesStringStringValue;
import com.navercorp.pinpoint.grpc.trace.PIntBooleanIntBooleanValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringStringValue;
import com.navercorp.pinpoint.grpc.trace.PIntStringValue;
import com.navercorp.pinpoint.grpc.trace.PLongIntIntByteByteStringValue;
import com.navercorp.pinpoint.grpc.trace.PStringStringValue;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.annotation.BooleanAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.ByteAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.BytesAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.DataTypeAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.DoubleAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.IntAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.LongAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.NullAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.ObjectAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.ShortAnnotation;
import com.navercorp.pinpoint.profiler.context.annotation.StringAnnotation;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Qualifier;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author intr3p1d
 */
@Mapper(
        subclassExhaustiveStrategy = SubclassExhaustiveStrategy.COMPILE_ERROR,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {
        }
)
public interface AnnotationValueMapper {

    PAnnotationValue.Builder annotationBuilder = PAnnotationValue.newBuilder();
    StringValue.Builder stringValueBuilder = StringValue.newBuilder();
    PIntBooleanIntBooleanValue.Builder intBoolBoolBuilder = PIntBooleanIntBooleanValue.newBuilder();
    PLongIntIntByteByteStringValue.Builder longIntIntByteByteStringBuilder = PLongIntIntByteByteStringValue.newBuilder();
    PIntStringStringValue.Builder intStringStringBuilder = PIntStringStringValue.newBuilder();
    PIntStringValue.Builder intStringBuilder = PIntStringValue.newBuilder();
    PStringStringValue.Builder stringStringBuilder = PStringStringValue.newBuilder();
    PBytesStringStringValue.Builder bytesStringStringBuilder = PBytesStringStringValue.newBuilder();

    default PAnnotationValue.Builder getAnnotationBuilder() {
        final PAnnotationValue.Builder builder = this.annotationBuilder;
        builder.clear();
        return builder;
    }

    default StringValue.Builder getStringValueBuilder() {
        final StringValue.Builder builder = this.stringValueBuilder;
        builder.clear();
        return builder;
    }

    default PIntBooleanIntBooleanValue.Builder getIntBoolIntBoolBuilder() {
        final PIntBooleanIntBooleanValue.Builder builder = this.intBoolBoolBuilder;
        builder.clear();
        return builder;
    }

    default PLongIntIntByteByteStringValue.Builder getLongIntIntByteByteStringBuilder() {
        final PLongIntIntByteByteStringValue.Builder builder = this.longIntIntByteByteStringBuilder;
        builder.clear();
        return builder;
    }

    default PIntStringStringValue.Builder getIntStringStringBuilder() {
        final PIntStringStringValue.Builder builder = this.intStringStringBuilder;
        builder.clear();
        return builder;
    }

    default PIntStringValue.Builder getIntStringBuilder() {
        final PIntStringValue.Builder builder = this.intStringBuilder;
        builder.clear();
        return builder;
    }

    default PStringStringValue.Builder getStringStringBuilder() {
        final PStringStringValue.Builder builder = this.stringStringBuilder;
        builder.clear();
        return builder;
    }

    default PBytesStringStringValue.Builder getBytesStringStringBuilder() {
        final PBytesStringStringValue.Builder builder = this.bytesStringStringBuilder;
        builder.clear();
        return builder;
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @interface ToPAnnotationValue {
    }

    @ToPAnnotationValue
    default PAnnotationValue map(Annotation<?> annotation) {
        if (annotation instanceof NullAnnotation) {
            PAnnotationValue.Builder builder = PAnnotationValue.newBuilder();
            builder.setStringValue("null");
            return builder.build();
        }
        return mapNonNull(annotation);
    }

    @Named("dummy")


    @Mapping(target = "binaryValue", ignore = true)
    @Mapping(target = "boolValue", ignore = true)
    @Mapping(target = "byteValue", ignore = true)
    @Mapping(target = "bytesStringStringValue", ignore = true)
    @Mapping(target = "doubleValue", ignore = true)
    @Mapping(target = "intBooleanIntBooleanValue", ignore = true)
    @Mapping(target = "intStringStringValue", ignore = true)
    @Mapping(target = "intStringValue", ignore = true)
    @Mapping(target = "intValue", ignore = true)
    @Mapping(target = "longIntIntByteByteStringValue", ignore = true)
    @Mapping(target = "longValue", ignore = true)
    @Mapping(target = "stringValue", ignore = true)
    @Mapping(target = "shortValue", ignore = true)
    @Mapping(target = "stringStringValue", ignore = true)
    PAnnotationValue dummyForIgnoreMapping(Annotation<?> annotation);


    @SubclassMapping(source = BooleanAnnotation.class, target = PAnnotationValue.class)
    @SubclassMapping(source = ByteAnnotation.class, target = PAnnotationValue.class)
    @SubclassMapping(source = BytesAnnotation.class, target = PAnnotationValue.class)
    @SubclassMapping(source = DataTypeAnnotation.class, target = PAnnotationValue.class)
    @SubclassMapping(source = DoubleAnnotation.class, target = PAnnotationValue.class)
    @SubclassMapping(source = IntAnnotation.class, target = PAnnotationValue.class)
    @SubclassMapping(source = LongAnnotation.class, target = PAnnotationValue.class)
    @SubclassMapping(source = ObjectAnnotation.class, target = PAnnotationValue.class)
    @SubclassMapping(source = ShortAnnotation.class, target = PAnnotationValue.class)
    @SubclassMapping(source = StringAnnotation.class, target = PAnnotationValue.class)
    @InheritConfiguration(name = "dummyForIgnoreMapping")
    PAnnotationValue mapNonNull(Annotation<?> annotation);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "booleanValue", target = "boolValue")
    PAnnotationValue map(BooleanAnnotation annotation);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "byteValue", target = "byteValue")
    PAnnotationValue map(ByteAnnotation annotation);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "value", target = "binaryValue", qualifiedByName = "copyFrom")
    PAnnotationValue map(BytesAnnotation annotation);

    @Named("copyFrom")
    default ByteString copyFrom(byte[] v) {
        return ByteString.copyFrom(v);
    }

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "doubleValue", target = "doubleValue")
    PAnnotationValue map(DoubleAnnotation annotation);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "intValue", target = "intValue")
    PAnnotationValue map(IntAnnotation annotation);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "longValue", target = "longValue")
    PAnnotationValue map(LongAnnotation annotation);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "value", target = "stringValue", qualifiedByName = "ObjectToString")
    PAnnotationValue map(ObjectAnnotation annotation);

    @Named("ObjectToString")
    default String objectToString(Object o) {
        return (String) o;
    }

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "shortValue", target = "shortValue")
    PAnnotationValue map(ShortAnnotation annotation);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "value", target = "stringValue")
    PAnnotationValue map(StringAnnotation annotation);

    default PAnnotationValue map(DataTypeAnnotation annotation) {
        PAnnotationValue.Builder builder = PAnnotationValue.newBuilder();

        final DataType dataType = annotation.getValue();
        if (dataType instanceof IntStringValue) {
            final IntStringValue v = (IntStringValue) dataType;
            PIntStringValue pIntStringValue = map((IntStringValue) dataType);
            builder.setIntStringValue(pIntStringValue);
            return builder.build();
        } else if (dataType instanceof StringStringValue) {
            final StringStringValue v = (StringStringValue) dataType;
            PStringStringValue pStringStringValue = map(v);
            builder.setStringStringValue(pStringStringValue);
            return builder.build();
        } else if (dataType instanceof IntStringStringValue) {
            final IntStringStringValue v = (IntStringStringValue) dataType;
            final PIntStringStringValue pIntStringStringValue = map(v);
            builder.setIntStringStringValue(pIntStringStringValue);
            return builder.build();
        } else if (dataType instanceof LongIntIntByteByteStringValue) {
            final LongIntIntByteByteStringValue v = (LongIntIntByteByteStringValue) dataType;
            final PLongIntIntByteByteStringValue pValue = map(v);
            builder.setLongIntIntByteByteStringValue(pValue);
            return builder.build();
        } else if (dataType instanceof IntBooleanIntBooleanValue) {
            final IntBooleanIntBooleanValue v = (IntBooleanIntBooleanValue) dataType;
            final PIntBooleanIntBooleanValue pValue = map(v);
            builder.setIntBooleanIntBooleanValue(pValue);
            return builder.build();
        } else if (dataType instanceof BytesStringStringValue) {
            final BytesStringStringValue v = (BytesStringStringValue) dataType;
            PBytesStringStringValue pValue = map(v);
            builder.setBytesStringStringValue(pValue);
            return builder.build();
        }
        throw new UnsupportedOperationException("unsupported type:" + dataType);
    }

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    PIntStringValue map(IntStringValue v);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    PStringStringValue map(StringStringValue v);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    PIntStringStringValue map(IntStringStringValue v);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "intValue2", target = "intValue2", conditionQualifiedByName = "isNotMinusInt")
    @Mapping(source = "byteValue1", target = "byteValue1", conditionQualifiedByName = "isNotMinusByte")
    @Mapping(source = "byteValue2", target = "byteValue2", conditionQualifiedByName = "isNotMinusByte")
    PLongIntIntByteByteStringValue map(LongIntIntByteByteStringValue v);


    @Named("isNotMinusInt")
    default boolean isNotMinusOne(int value) {
        return value != -1;
    }

    @Named("isNotMinusByte")
    default boolean isNotMinusOne(byte value) {
        return value != -1;
    }

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "booleanValue1", target = "boolValue1")
    @Mapping(source = "booleanValue2", target = "boolValue2")
    PIntBooleanIntBooleanValue map(IntBooleanIntBooleanValue v);

    @InheritConfiguration(name = "dummyForIgnoreMapping")
    @Mapping(source = "bytesValue", target = "bytesValue", qualifiedByName = "copyFrom")
    PBytesStringStringValue map(BytesStringStringValue v);


    @Mapping(source = ".", target = "value")
    StringValue map(String stringValue);


}
