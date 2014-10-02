package com.nhn.pinpoint.bootstrap.context;

import java.util.List;

/**
 * @author hyungil.jeong
 */
public interface ServerMetaData {
    String getServerInfo();

    List<String> getVmArgs();

    List<ServiceInfo> getServiceInfos();
}
