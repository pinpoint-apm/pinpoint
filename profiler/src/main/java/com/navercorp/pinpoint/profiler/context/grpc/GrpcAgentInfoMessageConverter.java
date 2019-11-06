/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PJvmGcType;
import com.navercorp.pinpoint.grpc.trace.PJvmInfo;
import com.navercorp.pinpoint.grpc.trace.PServerMetaData;
import com.navercorp.pinpoint.grpc.trace.PServiceInfo;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.metadata.AgentInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcAgentInfoMessageConverter implements MessageConverter<GeneratedMessageV3> {

    private final GrpcJvmGcTypeMessageConverter jvmGcTypeMessageConverter = new GrpcJvmGcTypeMessageConverter();

    @Override
    public GeneratedMessageV3 toMessage(Object message) {
        if (message instanceof AgentInfo) {
            final AgentInfo agentInfo = (AgentInfo) message;
            return convertAgentInfo(agentInfo);
        }
        return null;
    }


    public PAgentInfo convertAgentInfo(final AgentInfo agentInfo) {
        final AgentInformation agentInformation = agentInfo.getAgentInformation();

        final PAgentInfo.Builder builder = PAgentInfo.newBuilder();
        builder.setIp(agentInformation.getHostIp());
        builder.setHostname(agentInformation.getMachineName());
        builder.setPorts("");
        builder.setContainer(agentInformation.isContainer());
        builder.setPid(agentInformation.getPid());
        builder.setServiceType(agentInformation.getServerType().getCode());
        builder.setVmVersion(agentInformation.getJvmVersion());
        builder.setAgentVersion(Version.VERSION);

        final ServerMetaData serverMetaData = agentInfo.getServerMetaData();
        if (serverMetaData != null) {
            final PServerMetaData tServerMetaData = convertServerMetaData(agentInfo.getServerMetaData());
            builder.setServerMetaData(tServerMetaData);
        }

        final JvmInformation jvmInformation = agentInfo.getJvmInfo();
        if (jvmInformation != null) {
            final PJvmInfo tJvmInfo = convertJvmInfo(agentInfo.getJvmInfo());
            builder.setJvmInfo(tJvmInfo);
        }
        return builder.build();
    }

    private PServerMetaData convertServerMetaData(final ServerMetaData serverMetaData) {
        final PServerMetaData.Builder serverMetaDataBuilder = PServerMetaData.newBuilder();
        serverMetaDataBuilder.setServerInfo(serverMetaData.getServerInfo());
        serverMetaDataBuilder.addAllVmArg(serverMetaData.getVmArgs());
        final List<PServiceInfo> serviceInfoList = new ArrayList<PServiceInfo>();
        for (ServiceInfo serviceInfo : serverMetaData.getServiceInfos()) {
            final PServiceInfo.Builder serviceInfoBuilder = PServiceInfo.newBuilder();
            serviceInfoBuilder.setServiceName(serviceInfo.getServiceName());
            serviceInfoBuilder.addAllServiceLib(serviceInfo.getServiceLibs());
            serviceInfoList.add(serviceInfoBuilder.build());
        }
        serverMetaDataBuilder.addAllServiceInfo(serviceInfoList);
        return serverMetaDataBuilder.build();
    }

    private PJvmInfo convertJvmInfo(final JvmInformation jvmInformation) {
        final PJvmInfo.Builder builder = PJvmInfo.newBuilder();
        builder.setVmVersion(jvmInformation.getJvmVersion());
        PJvmGcType gcType = this.jvmGcTypeMessageConverter.toMessage(jvmInformation.getJvmGcType());
        builder.setGcType(gcType);
        return builder.build();
    }
}
