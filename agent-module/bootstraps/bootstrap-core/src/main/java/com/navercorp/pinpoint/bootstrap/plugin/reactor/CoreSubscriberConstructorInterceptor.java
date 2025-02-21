/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayUtils;

public class CoreSubscriberConstructorInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (throwable != null) {
            return;
        }

        try {
            if (target instanceof ReactorActualAccessor) {
                // Check actual subscriber
                final ReactorSubscriberAccessor actual = findActual(args);
                if (actual != null) {
                    ((ReactorActualAccessor) target)._$PINPOINT$_setReactorActual(actual);
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        }
    }

    public static ReactorSubscriberAccessor findActual(Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }
        final int length = args.length - 1;
        for (int i = 0; i <= length; i++) {
            final Object arg = args[i];
            if (arg instanceof ReactorSubscriberAccessor) {
                return (ReactorSubscriberAccessor) arg;
            }
        }
        return null;
    }
}