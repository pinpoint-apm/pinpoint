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

package com.navercorp.pinpoint.collector.mapper.grpc;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PJvmInfo;
import com.navercorp.pinpoint.grpc.trace.PServerMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author hyungil.jeong
 */
@Component
public class GrpcAgentInfoBoMapper {
    private final GrpcServerMetaDataBoMapper serverMetaDataBoMapper;

    private final GrpcJvmInfoBoMapper jvmInfoBoMapper;

    public GrpcAgentInfoBoMapper(GrpcServerMetaDataBoMapper serverMetaDataBoMapper, GrpcJvmInfoBoMapper jvmInfoBoMapper) {
        this.serverMetaDataBoMapper = Objects.requireNonNull(serverMetaDataBoMapper, "serverMetaDataBoMapper");
        this.jvmInfoBoMapper = Objects.requireNonNull(jvmInfoBoMapper, "jvmInfoBoMapper");
    }

    public AgentInfoBo map(final PAgentInfo agentInfo, final Header header) {
        final String agentId = header.getAgentId();
        final String applicationName = header.getApplicationName();
        final long startTime = header.getAgentStartTime();

        final String hostName = agentInfo.getHostname();
        final String ip = agentInfo.getIp();
        final String ports = agentInfo.getPorts();
        final short serviceType = (short) agentInfo.getServiceType();
        final int pid = agentInfo.getPid();
        final String vmVersion = agentInfo.getVmVersion();
        final String agentVersion = agentInfo.getAgentVersion();
        final long endTimeStamp = agentInfo.getEndTimestamp();
        final int endStatus = agentInfo.getEndStatus();
        final boolean container = agentInfo.getContainer();

        final AgentInfoBo.Builder builder = new AgentInfoBo.Builder();
        builder.setHostName(hostName);
        builder.setIp(ip);
        builder.setPorts(ports);
        builder.setAgentId(agentId);
        builder.setApplicationName(applicationName);
        builder.setServiceTypeCode(serviceType);
        builder.setPid(pid);
        builder.setVmVersion(vmVersion);
        builder.setAgentVersion(agentVersion);
        builder.setStartTime(startTime);
        builder.setEndTimeStamp(endTimeStamp);
        builder.setEndStatus(endStatus);
        builder.isContainer(container);

        if (agentInfo.hasServerMetaData()) {
            final PServerMetaData serverMetaData = agentInfo.getServerMetaData();
            builder.setServerMetaData(this.serverMetaDataBoMapper.map(serverMetaData));
        }

        if (agentInfo.hasJvmInfo()) {
            final PJvmInfo jvmInfo = agentInfo.getJvmInfo();
            builder.setJvmInfo(this.jvmInfoBoMapper.map(jvmInfo));
        }

        return builder.build();
    }
}