package com.nhn.pinpoint.collector.mapper.thrift;

import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;

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
        return new AgentInfoBo.Builder().hostName(hostName).ip(ip).ports(ports).agentId(agentId).applicationName(applicationName).serviceType(serviceType)
                .pid(pid).version(version).startTime(startTime).endTimeStamp(endTimeStamp).endStatus(endStatus).build();
    }

}
