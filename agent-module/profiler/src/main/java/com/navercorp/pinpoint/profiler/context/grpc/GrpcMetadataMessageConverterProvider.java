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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AgentInfoMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ExceptionMetaDataMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.MetaDataMapper;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class GrpcMetadataMessageConverterProvider implements Provider<MessageConverter<MetaDataType, GeneratedMessageV3>> {

    private final MetaDataMapper metaDataMapper;
    private final AgentInfoMapper agentInfoMapper;
    private final ExceptionMetaDataMapper exceptionMetaDataMapper;

    @Inject
    public GrpcMetadataMessageConverterProvider(
            MetaDataMapper metaDataMapper,
            AgentInfoMapper agentInfoMapper,
            ExceptionMetaDataMapper exceptionMetaDataMapper
    ) {
        this.metaDataMapper = Objects.requireNonNull(metaDataMapper, "metaDataMapper");
        this.agentInfoMapper = Objects.requireNonNull(agentInfoMapper, "agentInfoMapper");
        this.exceptionMetaDataMapper = Objects.requireNonNull(exceptionMetaDataMapper, "exceptionMetaDataMapper");
    }

    @Override
    public MessageConverter<MetaDataType, GeneratedMessageV3> get() {
        MessageConverter<MetaDataType, GeneratedMessageV3> metadataMessageConverter = new GrpcMetadataMessageConverter(metaDataMapper);
        MessageConverter<MetaDataType, GeneratedMessageV3> agentMessageConverter = new GrpcAgentInfoMessageConverter(agentInfoMapper);
        MessageConverter<MetaDataType, GeneratedMessageV3> exceptionMessageConverter = new GrpcExceptionMetaDataConverter(exceptionMetaDataMapper);

        return MessageConverterGroup.wrap(
                Arrays.asList(metadataMessageConverter, agentMessageConverter, exceptionMessageConverter)
        );
    }
}