/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.arcus;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;


public final class ArcusConstants {
    private ArcusConstants() {
    }

    public static final ServiceType ARCUS = ServiceTypeFactory.of(8100, "ARCUS", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_FUTURE_GET = ServiceTypeFactory.of(8101, "ARCUS_FUTURE_GET", "ARCUS", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_EHCACHE_FUTURE_GET = ServiceTypeFactory.of(8102, "ARCUS_EHCACHE_FUTURE_GET", "ARCUS-EHCACHE", TERMINAL, INCLUDE_DESTINATION_ID);
    public static final ServiceType ARCUS_INTERNAL = ServiceTypeFactory.of(8103, "ARCUS_INTERNAL", "ARCUS");
    
    public static final ServiceType MEMCACHED = ServiceTypeFactory.of(8050, "MEMCACHED", TERMINAL, RECORD_STATISTICS);
    public static final ServiceType MEMCACHED_FUTURE_GET = ServiceTypeFactory.of(8051, "MEMCACHED_FUTURE_GET", "MEMCACHED", TERMINAL);

    
    public static final String ARCUS_SCOPE = "ArcusScope";
    public static final String ARCUS_FUTURE_SCOPE = "ArcusFutureScope";
    public static final String ATTRIBUTE_CONFIG = "arcusPluginConfig";
    public static final String METADATA_SERVICE_CODE = "serviceCode";
    public static final String MEATDATA_CACHE_NAME = "cacheName";
    public static final String METADATA_CACHE_KEY = "cacheKey";
    public static final String METADATA_OPERATION = "operation";
    public static final String METADATA_ASYNC_CONTEXT = "asyncContext";
}
