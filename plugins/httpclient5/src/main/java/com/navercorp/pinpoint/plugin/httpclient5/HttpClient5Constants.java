package com.navercorp.pinpoint.plugin.httpclient5;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

public class HttpClient5Constants {

    public static final ServiceType HTTP_CLIENT5 = ServiceTypeProvider.getByName("HTTP_CLIENT_5");
    public static final ServiceType HTTP_CLIENT5_INTERNAL = ServiceTypeProvider.getByName("HTTP_CLIENT_5_INTERNAL");
}
