package com.navercorp.pinpoint.threadx;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

public class ThreadPoolConstants {
    public static final ServiceType THREAD_POOL_EXECUTOR = ServiceTypeFactory.of(5081, "THREAD_POOL_EXECUTOR", "THREAD_POOL_EXECUTOR");
    public static final String THREAD_POOL_EXECUTOR_SCOPE = "ThreadPoolExecutorScope";
}
