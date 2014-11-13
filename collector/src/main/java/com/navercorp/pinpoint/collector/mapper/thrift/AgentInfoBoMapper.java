package com.nhn.pinpoint.collector.mapper.thrift;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;
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
