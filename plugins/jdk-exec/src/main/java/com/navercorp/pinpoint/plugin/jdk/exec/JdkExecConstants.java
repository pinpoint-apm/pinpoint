package com.navercorp.pinpoint.plugin.jdk.exec;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author lisn
 */
public class JdkExecConstants {
    private JdkExecConstants() {
    }
    public static final String ASYNC_ID_MAP = "AsyncIdMap";
    public static final ServiceType SERVICE_TYPE = ServiceTypeFactory.of(7500, "JDK_EXEC", "JDK_EXEC");
}
