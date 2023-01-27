/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.redis.lettuce.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.redis.lettuce.EndPointAccessor;

public class RedisCluserClientConnectStatefulInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public RedisCluserClientConnectStatefulInterceptor() {
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
            if (Boolean.FALSE == (target instanceof EndPointAccessor)) {
                return;
            }
            final EndPointAccessor endPointAccessor = ArrayArgumentUtils.getArgument(args, 0, EndPointAccessor.class);
            if (endPointAccessor != null) {
                // Attach endPoint
                final String endPoint = ((EndPointAccessor) target)._$PINPOINT$_getEndPoint();
                endPointAccessor._$PINPOINT$_setEndPoint(endPoint);
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to AFTER process. {}", t.getMessage(), t);
            }
        }
    }
}
