package com.navercorp.pinpoint.plugin.redis;

import static com.navercorp.pinpoint.common.HistogramSchema.FAST_SCHEMA;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.ServiceTypeProperty.TERMINAL;

import com.navercorp.pinpoint.common.ServiceType;

public interface RedisConstants {

    public static final ServiceType REDIS = ServiceType.of(8200, "REDIS", FAST_SCHEMA, TERMINAL, RECORD_STATISTICS);
    public static final String METADATA_END_POINT = "endPoint";
    public static final String METADATA_DESTINATION_ID = "destinationId";
}
