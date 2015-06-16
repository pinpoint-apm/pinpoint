package com.navercorp.pinpoint.plugin.arcus;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher.*;
import static com.navercorp.pinpoint.common.trace.HistogramSchema.*;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;



public interface ArcusConstants {
    public static final ServiceType ARCUS = ServiceType.of(8100, "ARCUS", FAST_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_FUTURE_GET = ServiceType.of(8101, "ARCUS_FUTURE_GET", "ARCUS", FAST_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_EHCACHE_FUTURE_GET = ServiceType.of(8102, "ARCUS_EHCACHE_FUTURE_GET", "ARCUS-EHCACHE", FAST_SCHEMA, TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_INTERNAL = ServiceType.of(8103, "ARCUS_INTERNAL", "ARCUS", FAST_SCHEMA);

    
    public static final String ARCUS_SCOPE = "ArcusScope";
    public static final String ARCUS_FUTURE_SCOPE = "ArcusFutureScope";
    public static final String ATTRIBUTE_CONFIG = "arcusPluginConfig";
    public static final String METADATA_SERVICE_CODE = "serviceCode";
    public static final String MEATDATA_CACHE_NAME = "cacheName";
    public static final String METADATA_CACHE_KEY = "cacheKey";
    public static final String METADATA_OPERATION = "operation";
    public static final String METADATA_ASYNC_TRACE_ID = "asyncTraceId";
}
