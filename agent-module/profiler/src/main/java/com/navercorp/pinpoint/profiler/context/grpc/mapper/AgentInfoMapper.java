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

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PJvmInfo;
import com.navercorp.pinpoint.grpc.trace.PServerMetaData;
import com.navercorp.pinpoint.grpc.trace.PServiceInfo;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.metadata.AgentInfo;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

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
                JvmGcTypeMapper.class,
        }
)
public interface AgentInfoMapper {

    @Mappings({
            @Mapping(source = "agentInformation.hostIp", target = "ip"),
            @Mapping(source = "agentInformation.machineName", target = "hostname"),
            @Mapping(source = ".", target = "ports", qualifiedByName = "emptyPort"),
            @Mapping(source = "agentInformation.container", target = "container"),
            @Mapping(source = "agentInformation.pid", target = "pid"),
            @Mapping(source = "agentInformation.serverType.code", target = "serviceType"),
            @Mapping(source = "agentInformation.jvmVersion", target = "vmVersion"),
            @Mapping(source = ".", target = "agentVersion", qualifiedByName = "agentVersion")
    })
    PAgentInfo map(AgentInfo agentInfo);

    @Mappings({
            @Mapping(source = "vmArgs", target = "vmArgList"),
            @Mapping(source = "serviceInfos", target = "serviceInfoList"),
    })
    PServerMetaData map(ServerMetaData serverMetaData);

    @Mappings({
            @Mapping(source = "serviceLibs", target = "serviceLibList"),
    })
    PServiceInfo map(ServiceInfo serviceInfo);

    @Mappings({
            @Mapping(source = "jvmVersion", target = "vmVersion"),
            @Mapping(source = "jvmGcType", target = "gcType", qualifiedBy = JvmGcTypeMapper.ToPJvmGcType.class)

    })
    PJvmInfo map(JvmInformation jvmInformation);

    @Named("emptyPort")
    default String emptyPort(AgentInfo agentInfo) {
        return "";
    }

    @Named("agentVersion")
    default String agentVersion(AgentInfo agentInfo) {
        return Version.VERSION;
    }

}
