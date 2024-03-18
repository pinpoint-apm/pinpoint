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
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaData;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaData;
import com.navercorp.pinpoint.profiler.metadata.SqlUidMetaData;
import com.navercorp.pinpoint.profiler.metadata.StringMetaData;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.SubclassMapping;
import org.mapstruct.SubclassMappings;

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
public interface MetaDataMapper {

    // FIXME
    // Currently, Cannot use GeneratedMessageV3 for @SubClassMapping
    // It requires a non abstract / non interface result type or a factory method
    default GeneratedMessageV3 map(MetaDataType metaDataType) {
        return (GeneratedMessageV3) mapSubClass(metaDataType);
    }

    @SubclassMappings({
            @SubclassMapping(source = SqlMetaData.class, target = PSqlMetaData.class),
            @SubclassMapping(source = SqlUidMetaData.class, target = PSqlUidMetaData.class),
            @SubclassMapping(source = ApiMetaData.class, target = PApiMetaData.class),
            @SubclassMapping(source = StringMetaData.class, target = PStringMetaData.class),
    })
    Object mapSubClass(MetaDataType metaDataType);

    PSqlMetaData map(SqlMetaData sqlMetaData);

    PSqlUidMetaData map(SqlUidMetaData sqlUidMetaData);

    PApiMetaData map(ApiMetaData apiMetaData);

    PStringMetaData map(StringMetaData stringMetaData);

    default ByteString map(byte[] bytes) {
        return ByteString.copyFrom(bytes);
    }
}
