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
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue2Utils;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import net.sf.ehcache.CacheManager;
import net.spy.memcached.MemcachedClient;


/**
 * 
 * @author netspider
 * @author emeroad
 */
public class SetCacheManagerInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final String serviceCode = getServiceCode(args[0]);
        setServiceCode(target, serviceCode);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }

//    serviceCode->ObjectTraceValue2
    private String getServiceCode(Object target) {
        if (target instanceof MemcachedClient) {
            final Object serviceCodeObject = ObjectTraceValue2Utils.__getTraceObject2(target, null);
            if (serviceCodeObject instanceof String) {
                return (String) serviceCodeObject;
            }
        }
        return null;
    }


    private void setServiceCode(Object target, Object value) {
        if (target instanceof CacheManager) {
            ObjectTraceValue2Utils.__setTraceObject2(target, value);
        }
    }
}
