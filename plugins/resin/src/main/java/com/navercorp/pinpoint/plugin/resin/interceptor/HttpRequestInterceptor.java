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
package com.navercorp.pinpoint.plugin.resin.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.resin.AsyncAccessor;
import com.navercorp.pinpoint.plugin.resin.TraceAccessor;

/**
 * 
 * @author huangpengjie@fang.com
 *
 */
public class HttpRequestInterceptor implements AroundInterceptor {

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private InstrumentMethod targetMethod;

    public HttpRequestInterceptor(InstrumentMethod targetMethod) {
        this.targetMethod = targetMethod;
    }

    @Override
    public void before(Object target, Object[] args) {
        logger.beforeInterceptor(target, target.getClass().getName(), targetMethod.getName(), "", args);
        try {
            if (target != null) {
                if (target instanceof AsyncAccessor) {
                    // reset
                    ((AsyncAccessor) target)._$PINPOINT$_setAsync(Boolean.FALSE);
                }

                if (target instanceof TraceAccessor) {
                    final Trace trace = ((TraceAccessor) target)._$PINPOINT$_getTrace();
                    if (trace != null && trace.canSampled()) {
                        // end of root span
                        trace.close();
                    }
                    // reset
                    ((TraceAccessor) target)._$PINPOINT$_setTrace(null);
                }
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}