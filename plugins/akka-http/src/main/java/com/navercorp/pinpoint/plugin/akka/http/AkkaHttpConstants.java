package com.navercorp.pinpoint.plugin.akka.http;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;

/**
 * @author lopiter
 */
public class AkkaHttpConstants {
    public static final ServiceType AKKA_HTTP = ServiceTypeFactory.of(9999, "AKKA_HTTP_SERVER", ServiceTypeProperty.RECORD_STATISTICS);
    public static final ServiceType AKKA_HTTP_SERVER_INTERNAL = ServiceTypeFactory.of(9998, "AKKA_HTTP_SERVER_INTERNAL", "AKKA_HTTP_SERVER_INTERNAL");
}
