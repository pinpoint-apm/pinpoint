/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.MetaDataMapper;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaData;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaData;
import com.navercorp.pinpoint.profiler.metadata.SqlUidMetaData;
import com.navercorp.pinpoint.profiler.metadata.StringMetaData;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class GrpcMetadataMessageConverter implements MessageConverter<MetaDataType, GeneratedMessageV3> {

    private final MetaDataMapper mapper;

    public GrpcMetadataMessageConverter(
            MetaDataMapper metaDataMapper
    ) {
        this.mapper = Objects.requireNonNull(metaDataMapper, "metaDataMapper");
    }

    @Override
    public GeneratedMessageV3 toMessage(MetaDataType message) {
        try {
            return mapper.map(message);
        } catch (Exception e) {
            return null;
        }
    }
}