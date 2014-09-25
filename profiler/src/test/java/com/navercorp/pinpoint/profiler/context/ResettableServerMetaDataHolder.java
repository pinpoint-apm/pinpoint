package com.nhn.pinpoint.profiler.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.nhn.pinpoint.bootstrap.context.ServerMetaData;
import com.nhn.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.nhn.pinpoint.bootstrap.context.ServiceInfo;

/**
 * @author hyungil.jeong
 */
public class ResettableServerMetaDataHolder implements ServerMetaDataHolder {
    
    private String serverName;
    private String vmArgs;
    private Queue<ServiceInfo> serviceInfos;
    
    public ResettableServerMetaDataHolder() {
        this.serviceInfos = new ConcurrentLinkedQueue<ServiceInfo>();
    }

    @Override
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void setVmArgs(String vmArgs) {
        this.vmArgs = vmArgs;
    }

    @Override
    public void addServiceInfo(String serviceName, List<String> serviceLibs) {
        this.serviceInfos.add(new DefaultServiceInfo(serviceName, serviceLibs));
    }

    @Override
    public ServerMetaData getServerMetaData() {
        ServerMetaData serverMetaData = new DefaultServerMetaData(this.serverName, this.vmArgs, new ArrayList<ServiceInfo>(this.serviceInfos));
        return serverMetaData;
    }
    
    public void reset() {
        this.serverName = null;
        this.vmArgs = null;
        this.serviceInfos = new ConcurrentLinkedQueue<ServiceInfo>();
    }

}
