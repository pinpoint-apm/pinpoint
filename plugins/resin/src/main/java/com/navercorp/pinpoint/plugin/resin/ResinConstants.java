package com.navercorp.pinpoint.plugin.resin;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author huangpengjie@fang.com
 */
public interface ResinConstants {
    public static final ServiceType RESIN = ServiceTypeFactory.of(1200, "RESIN", RECORD_STATISTICS);
    public static final ServiceType RESIN_METHOD = ServiceTypeFactory.of(1201, "RESIN_METHOD");
}