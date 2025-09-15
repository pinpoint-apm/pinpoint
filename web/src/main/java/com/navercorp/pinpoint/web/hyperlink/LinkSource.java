package com.navercorp.pinpoint.web.hyperlink;

import com.navercorp.pinpoint.common.trace.ServiceType;

public interface LinkSource {
    String getHostName();
    String getIp();
    ServiceType getServiceType();
}
