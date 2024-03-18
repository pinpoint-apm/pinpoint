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

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.arcus.ServiceCodeAccessor;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class SetCacheManagerInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    
    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Object serviceCodeObject = args[0];
        if (serviceCodeObject instanceof ServiceCodeAccessor && target instanceof ServiceCodeAccessor) {
            String serviceCode = ((ServiceCodeAccessor) serviceCodeObject)._$PINPOINT$_getServiceCode();
            ((ServiceCodeAccessor) target)._$PINPOINT$_setServiceCode(serviceCode);
        }
    }

// #1375 Workaround java level Deadlock
// https://oss.navercorp.com/pinpoint/pinpoint-naver/issues/1375
//    @IgnoreMethod
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }
}
