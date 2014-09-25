package com.nhn.pinpoint.bootstrap.context;

import java.util.List;

/**
 * @author hyungil.jeong
 */
public interface ServerMetaData {
    String getServerInfo();

    String getVmArgs();

    List<ServiceInfo> getServiceInfos();
}
