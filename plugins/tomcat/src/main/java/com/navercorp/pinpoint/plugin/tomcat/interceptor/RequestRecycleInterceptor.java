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
package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConstants;

/**
 * 
 * @author jaehong.kim
 *
 */
public class RequestRecycleInterceptor implements SimpleAroundInterceptor, TomcatConstants {

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MethodInfo targetMethod;
    private MetadataAccessor traceAccessor;
    private MetadataAccessor asyncAccessor;

    public RequestRecycleInterceptor(MethodInfo targetMethod, @Name(METADATA_TRACE) MetadataAccessor traceAccessor, @Name(METADATA_ASYNC) MetadataAccessor asyncAccessor) {
        this.targetMethod = targetMethod;
        this.traceAccessor = traceAccessor;
        this.asyncAccessor = asyncAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        logger.beforeInterceptor(target, target.getClass().getName(), targetMethod.getName(), "", args);
        try {
            if (asyncAccessor.isApplicable(target)) {
                // reset
                asyncAccessor.set(target, Boolean.FALSE);
            }

            if (traceAccessor.isApplicable(target) && traceAccessor.get(target) != null) {
                final Trace trace = traceAccessor.get(target);
                if (trace != null && trace.canSampled()) {
                    // end of root span
                    trace.close();
                }
                // reset
                traceAccessor.set(target, null);
            }
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}