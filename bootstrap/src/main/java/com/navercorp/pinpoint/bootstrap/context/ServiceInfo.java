package com.nhn.pinpoint.bootstrap.context;

import java.util.List;

/**
 * @author hyungil.jeong
 */
public interface ServiceInfo {
    String getServiceName();
    
    List<String> getServiceLibs();
}
