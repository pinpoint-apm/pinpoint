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

public class DefaultPreparedStatementInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;

    public DefaultPreparedStatementInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
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

        if (Boolean.FALSE == (result instanceof BindValueAccessor)) {
            return;
        }

        Map<Integer, String> bindMap = new HashMap<>();
        int bindIndex = 1;

        try {
            final Object[] values = ArrayArgumentUtils.getArgument(args, 0, Object[].class);
            if (values != null) {
                for (Object value : values) {
                    final String bind = traceContext.getJdbcContext().getBindVariableService().formatBindVariable(value);
                    if (bind != null) {
                        bindMap.put(bindIndex++, bind);
                    }
                }
            }
            ((BindValueAccessor) result)._$PINPOINT$_setBindValue(bindMap);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        }
    }
}