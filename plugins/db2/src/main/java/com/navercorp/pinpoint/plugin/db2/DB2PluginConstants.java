package com.navercorp.pinpoint.plugin.db2;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;


public final class DB2PluginConstants {
    private DB2PluginConstants() {
    }

    public static final String DB2_SCOPE = "DB2_JDBC";

    public static final ServiceType DB2 = ServiceTypeFactory.of(2900, "DB2", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType DB2_EXECUTE_QUERY = ServiceTypeFactory.of(2901, "DB2_EXECUTE_QUERY",
            "DB2", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
}
