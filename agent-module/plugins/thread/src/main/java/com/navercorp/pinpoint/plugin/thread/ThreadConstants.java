package com.navercorp.pinpoint.plugin.thread;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author echo
 */
public class ThreadConstants {

    public static final String SCOPE_NAME = "THREAD_ASYNC";

    public static final ServiceType SERVICE_TYPE = ServiceTypeFactory.of(6001, SCOPE_NAME);
}
