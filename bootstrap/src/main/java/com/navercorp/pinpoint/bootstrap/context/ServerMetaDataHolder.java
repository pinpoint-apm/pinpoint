package com.nhn.pinpoint.bootstrap.context;

import java.util.List;

/**
 * @author hyungil.jeong
 */
public interface ServerMetaDataHolder {
    void setServerName(String serverName);
    
    void addServiceInfo(String serviceName, List<String> serviceLibs);
    
    ServerMetaData getServerMetaData();
}
