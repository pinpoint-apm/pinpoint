/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ktor.interceptor;

import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;

public class ConfigureRoutingFactoryInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final Instrumentor instrumentor;
    private final TransformCallback transformer;

    public ConfigureRoutingFactoryInterceptor(Instrumentor instrumentor, TransformCallback transformer) {
        this.instrumentor = instrumentor;
        this.transformer = transformer;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return;
        }

        Object object = findHandleObject(args);
        if (object == null) {
            return;
        }

        processBean(object);
    }

    Object findHandleObject(Object[] args) {
        return ArrayArgumentUtils.getArgument(args, 0, Object.class);
    }

    public final void processBean(Object bean) {
        Class<?> clazz = bean.getClass();

        // If you want to trace inherited methods, you have to retranform super classes, too.
        instrumentor.retransform(clazz, transformer);
        if (isDebug) {
            logger.debug("Retransform class=" + clazz.getName());
        }
    }
}