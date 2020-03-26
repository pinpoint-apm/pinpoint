/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.mapper.thrift;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author hyungil.jeong
 */
@Component
public class ThriftAgentInfoBoMapper {
    private final ThriftServerMetaDataBoMapper serverMetaDataBoMapper;

    private final ThriftJvmInfoBoMapper jvmInfoBoMapper;

    public ThriftAgentInfoBoMapper(ThriftServerMetaDataBoMapper serverMetaDataBoMapper, ThriftJvmInfoBoMapper jvmInfoBoMapper) {
        this.serverMetaDataBoMapper = Objects.requireNonNull(serverMetaDataBoMapper, "serverMetaDataBoMapper");
        this.jvmInfoBoMapper = Objects.requireNonNull(jvmInfoBoMapper, "jvmInfoBoMapper");
    }


    public AgentInfoBo map(TAgentInfo thriftObject) {
        final String hostName = thriftObject.getHostname();
        final String ip = thriftObject.getIp();
        final String ports = thriftObject.getPorts();
        final String agentId = thriftObject.getAgentId();
        final String applicationName = thriftObject.getApplicationName();
        final short serviceType = thriftObject.getServiceType();
        final int pid = thriftObject.getPid();
        final String vmVersion = thriftObject.getVmVersion();
        final String agentVersion = thriftObject.getAgentVersion();
        final long startTime = thriftObject.getStartTimestamp();
        final long endTimeStamp = thriftObject.getEndTimestamp();
        final int endStatus = thriftObject.getEndStatus();
        final boolean container = thriftObject.isContainer();

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

        if (thriftObject.isSetServerMetaData()) {
            builder.setServerMetaData(this.serverMetaDataBoMapper.map(thriftObject.getServerMetaData()));
        }

        if (thriftObject.isSetJvmInfo()) {
            builder.setJvmInfo(this.jvmInfoBoMapper.map(thriftObject.getJvmInfo()));
        }

        return builder.build();
    }
}
