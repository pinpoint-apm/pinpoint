package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

public class OpenwhiskConstants {
    public static final ServiceType OPENWHISK_INTERNAL = ServiceTypeFactory.of(9997, "OPENWHISK_INTERNAL", "OPENWHISK_INTERNAL");
    public static final ServiceType OPENWHISK_INVOKER = ServiceTypeFactory.of(9996, "OPENWHISK_INVOKER", "OPENWHISK_INVOKER", RECORD_STATISTICS);

    public static final String PINPOINT_HEADER_DELIMITIER = "@";
    public static final int PINPOINT_HEADER_DELIMITIER_ASCII = PINPOINT_HEADER_DELIMITIER.charAt(0);
    public static final String PINPOINT_HEADER_PREFIX = PINPOINT_HEADER_DELIMITIER + "pinpoint_start";
    public static final int PINPOINT_HEADER_PREFIX_LENGTH = PINPOINT_HEADER_PREFIX.length() + 1; // include after '@'
    public static final String PINPOINT_HEADER_POSTFIX = PINPOINT_HEADER_DELIMITIER + "pinpoint_end";
    public static final int PINPOINT_HEADER_POSTFIX_LENGTH = PINPOINT_HEADER_POSTFIX.length();

    public static final int PINPOINT_HEADER_COUNT = 6;
    public static final int TRACE_ID = 0;
    public static final int PARENT_SPAN_ID = 1;
    public static final int SPAN_ID = 2;
    public static final int PARENT_APPLICATION_NAME = 3;
    public static final int PARENT_APPLICATION_TYPE = 4;
    public static final int FLAGS = 5;
}
