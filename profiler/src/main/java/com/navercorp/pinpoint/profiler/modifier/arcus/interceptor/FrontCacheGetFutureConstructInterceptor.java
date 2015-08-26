/*
 * Copyright 2014 NAVER Corp.
 *
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

package com.navercorp.pinpoint.profiler.modifier.arcus.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue3Utils;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue4Utils;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import net.sf.ehcache.Element;

/**
 * @author harebox
 */
public class FrontCacheGetFutureConstructInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    // TODO This should be extracted from FrontCacheMemcachedClient.
    private static final String DEFAULT_FRONTCACHE_NAME = "front";



    @Override
    public void before(Object target, Object[] args) {
        // do nothing.
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            setCacheName(target, DEFAULT_FRONTCACHE_NAME);

            if (args[0] instanceof Element) {
                final Element element = (Element) args[0];
                setCacheKey(target, element.getObjectKey());
            }
        } catch (Exception e) {
            logger.error("failed to add metadata: {}", e);
        }
    }

//    __cacheName->ObjectTrace3
    private void setCacheName(Object target, Object value) {
        ObjectTraceValue3Utils.__setTraceObject3(target, value);
    }

//    __cacheKey->ObjectTrace4
    private void setCacheKey(Object target, Object value) {
        ObjectTraceValue4Utils.__setTraceObject4(target, value);
    }
}
