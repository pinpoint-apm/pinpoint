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

import com.navercorp.pinpoint.grpc.trace.PJvmGcType;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.JvmGcType;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.EnumMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Qualifier;
import org.mapstruct.ValueMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author intr3p1d
 */
@Mapper(
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {
        }
)
public interface JvmGcTypeMapper {

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @interface ToPJvmGcType {
    }

    @ToPJvmGcType
    @EnumMapping(nameTransformationStrategy = "prefix", configuration = "JVM_GC_TYPE_")
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = "JVM_GC_TYPE_UNKNOWN")
    PJvmGcType map(JvmGcType message);
}
