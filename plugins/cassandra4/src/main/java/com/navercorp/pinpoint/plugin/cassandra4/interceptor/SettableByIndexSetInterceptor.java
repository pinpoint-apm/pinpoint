/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.cassandra4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;

import java.util.HashMap;
import java.util.Map;

public class SettableByIndexSetInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;

    public SettableByIndexSetInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return;
        }

        if (Boolean.FALSE == (target instanceof BindValueAccessor)) {
            return;
        }

        final BindValueAccessor bindValueAccessor = (BindValueAccessor) target;
        Map<Integer, String> bindMap = bindValueAccessor._$PINPOINT$_getBindValue();
        if (bindMap == null) {
            bindMap = new HashMap<>();
        }

        try {
            final Integer index = ArrayArgumentUtils.getArgument(args, 0, Integer.class);
            if (index == null) {
                return;
            }

            final Object value = ArrayArgumentUtils.getArgument(args, 1, Object.class);
            if (value == null) {
                return;
            }

            final String bind = traceContext.getJdbcContext().getBindVariableService().formatBindVariable(value);
            if (bind != null) {
                bindMap.put(index, bind);
            }
            bindValueAccessor._$PINPOINT$_setBindValue(bindMap);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        }
    }
}