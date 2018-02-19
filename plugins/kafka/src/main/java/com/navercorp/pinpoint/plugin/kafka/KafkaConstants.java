package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

public class KafkaConstants {
    public static final ServiceType KAFKA = ServiceTypeFactory.of(9995, "KAFKA", "KAFKA", RECORD_STATISTICS);
    public static final String PINPOINT_HEADER_DELIMITIER = "@";
    public static final String PINPOINT_HEADER_PREFIX = PINPOINT_HEADER_DELIMITIER + "pinpoint_start" + PINPOINT_HEADER_DELIMITIER;
    public static final String PINPOINT_HEADER_POSTFIX = PINPOINT_HEADER_DELIMITIER + "pinpoint_end";


}
