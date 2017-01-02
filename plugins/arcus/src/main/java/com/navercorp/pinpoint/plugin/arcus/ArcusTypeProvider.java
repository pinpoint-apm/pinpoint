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

import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author Jongho Moon
 *
 */
public class ArcusTypeProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(ArcusConstants.ARCUS, AnnotationKeyMatchers.ARGS_MATCHER);
        context.addServiceType(ArcusConstants.ARCUS_FUTURE_GET);
        context.addServiceType(ArcusConstants.ARCUS_EHCACHE_FUTURE_GET);
        context.addServiceType(ArcusConstants.MEMCACHED, AnnotationKeyMatchers.ARGS_MATCHER);
        context.addServiceType(ArcusConstants.MEMCACHED_FUTURE_GET);
    }
}
