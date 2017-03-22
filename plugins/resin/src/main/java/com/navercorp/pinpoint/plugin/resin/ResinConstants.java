package com.navercorp.pinpoint.plugin.resin;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * 
 * @author huangpengjie@fang.com
 *
 */
public interface ResinConstants {

    public static final ServiceType RESIN = ServiceTypeFactory.of(1200, "RESIN", RECORD_STATISTICS);
    public static final ServiceType RESIN_METHOD = ServiceTypeFactory.of(1201, "RESIN_METHOD");

    public static final String RESIN_SERVLET_SCOPE = "ResinServletScope";

    public static final String RESIN_SERVLET_ASYNC_SCOPE = "ResinServletAsyncScope";

    public static final String ASYNC_ACCESSOR = "com.navercorp.pinpoint.plugin.resin.AsyncAccessor";
    public static final String TRACE_ACCESSOR = "com.navercorp.pinpoint.plugin.resin.TraceAccessor";

    public static final String VERSION_ACCESSOR = "com.navercorp.pinpoint.plugin.resin.VersionAccessor";

}
