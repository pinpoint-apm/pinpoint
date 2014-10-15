package com.nhn.pinpoint.profiler.context;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.nhn.pinpoint.bootstrap.context.ServerMetaData;
import com.nhn.pinpoint.bootstrap.context.ServiceInfo;

/**
 * @author hyungil.jeong
 */
public class DefaultServerMetaData implements ServerMetaData {
    
    private final String serverInfo;
    private final List<String> vmArgs;
    private final Map<Integer, String> connectors;
    private final List<ServiceInfo> serviceInfo;

    public DefaultServerMetaData(String serverInfo, List<String> vmArgs, Map<Integer, String> connectors, List<ServiceInfo> serviceInfo) {
        this.serverInfo = serverInfo;
        this.vmArgs = vmArgs;
        this.connectors = connectors;
        this.serviceInfo = serviceInfo;
    }
    
    @Override
    public String getServerInfo() {
        return this.serverInfo;
    }

    @Override
    public List<String> getVmArgs() {
        return Collections.unmodifiableList(this.vmArgs);
    }

    @Override
    public Map<Integer, String> getConnectors() {
        return Collections.unmodifiableMap(this.connectors);
    }

    @Override
    public List<ServiceInfo> getServiceInfos() {
        return Collections.unmodifiableList(this.serviceInfo);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultServerMetaData{");
        sb.append("serverInfo='").append(serverInfo).append('\'');
        sb.append(", vmArgs=").append(vmArgs);
        sb.append(", connectors=").append(connectors);
        sb.append(", serviceInfo=").append(serviceInfo).append('}');
        return sb.toString();
    }

}
