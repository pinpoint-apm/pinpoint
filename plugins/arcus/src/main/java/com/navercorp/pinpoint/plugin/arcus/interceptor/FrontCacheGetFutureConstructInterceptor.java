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
package com.navercorp.pinpoint.plugin.arcus.interceptor;

import net.sf.ehcache.Element;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.arcus.CacheKeyAccessor;
import com.navercorp.pinpoint.plugin.arcus.CacheNameAccessor;

/**
 * @author harebox
 */
public class FrontCacheGetFutureConstructInterceptor implements AroundInterceptor {

    // TODO This should be extracted from FrontCacheMemcachedClient.
    private static final String DEFAULT_FRONTCACHE_NAME = "front";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        // do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            if (target instanceof CacheNameAccessor) {
                ((CacheNameAccessor) target)._$PINPOINT$_setCacheName(DEFAULT_FRONTCACHE_NAME);
            }

            final Object elementArg = args[0];
            if (elementArg instanceof Element) {
                final Element element = (Element) elementArg;
                if (target instanceof CacheKeyAccessor) {
                    ((CacheKeyAccessor) target)._$PINPOINT$_setCacheKey(element.getObjectKey());
                }
            }
        } catch (Exception e) {
            logger.error("failed to add metadata: {}", e);
        }
    }
}
