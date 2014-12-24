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

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;

import org.springframework.stereotype.Component;

/**
 * @author hyungil.jeong
 */
@Component
public class AgentInfoBoMapper implements ThriftBoMapper<AgentInfoBo, TAgentInfo> {

    @Override
    public AgentInfoBo map(TAgentInfo thriftObject) {
        final String hostName = thriftObject.getHostname();
        final String ip = thriftObject.getIp();
        final String ports = thriftObject.getPorts();
        final String agentId = thriftObject.getAgentId();
        final String applicationName = thriftObject.getApplicationName();
        final ServiceType serviceType = ServiceType.findServiceType(thriftObject.getServiceType());
        final int pid = thriftObject.getPid();
        final String version = thriftObject.getVersion();
        final long startTime = thriftObject.getStartTimestamp();
        final long endTimeStamp = thriftObject.getEndTimestamp();
        final int endStatus = thriftObject.getEndStatus();

        AgentInfoBo.Builder builder = new AgentInfoBo.Builder();
        builder.hostName(hostName);
        builder.ip(ip);
        builder.ports(ports);
        builder.agentId(agentId);
        builder.applicationName(applicationName);
        builder.serviceType(serviceType);
        builder.pid(pid);
        builder.version(version);
        builder.startTime(startTime);
        builder.endTimeStamp(endTimeStamp);
        builder.endStatus(endStatus);

        return builder.build();
    }

}
