/**
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

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetMethod;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;

/**
 * 
 * @author netspider
 * @author emeroad
 */
@TargetMethod(name="setCacheManager", paramTypes="net.spy.memcached.CacheManager")
public class SetCacheManagerInterceptor implements SimpleAroundInterceptor, ArcusConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final MetadataAccessor serviceCodeAccessor;
    
    public SetCacheManagerInterceptor(@Name(METADATA_SERVICE_CODE) MetadataAccessor serviceCodeAccessor) {
        this.serviceCodeAccessor = serviceCodeAccessor;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // do nothing
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        String serviceCode = serviceCodeAccessor.get(args[0]);
        serviceCodeAccessor.set(target, serviceCode);
    }
}
