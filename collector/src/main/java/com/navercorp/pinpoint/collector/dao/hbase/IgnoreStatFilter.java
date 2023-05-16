package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.common.trace.ServiceType;

public interface IgnoreStatFilter {
    boolean filter(ServiceType calleeServiceType, String callHost);
}
