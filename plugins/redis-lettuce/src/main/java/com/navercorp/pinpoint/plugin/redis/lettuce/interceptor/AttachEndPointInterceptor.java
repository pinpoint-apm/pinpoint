/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.redis.lettuce.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.redis.lettuce.EndPointAccessor;

/**
 * @author jaehong.kim
 */
public class AttachEndPointInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;

    public AttachEndPointInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            if (!validate(target, result)) {
                return;
            }

            final String endPoint = ((EndPointAccessor) target)._$PINPOINT$_getEndPoint();
            ((EndPointAccessor) result)._$PINPOINT$_setEndPoint(endPoint);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to AFTER process. {}", t.getMessage(), t);
            }
        }
    }

    private boolean validate(final Object target, final Object result) {
        if (!(target instanceof EndPointAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor={}, target={}", EndPointAccessor.class.getName(), target);
            }
            return false;
        }

        if (!(result instanceof EndPointAccessor)) {
            if (isDebug) {
                logger.debug("Invalid result object. Need field accessor={}, result={}", EndPointAccessor.class.getName(), result);
            }
            return false;
        }
        return true;
    }
}