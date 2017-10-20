/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor;

import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.bindvalue.BindValueConverter;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;

/**
 * @author emeroad
 * @author jaehong.kim
 */
// #1375 Workaround java level Deadlock
// https://oss.navercorp.com/pinpoint/pinpoint-naver/issues/1375
//@TargetFilter(type = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter", singleton = true)
public class PreparedStatementBindVariableInterceptor implements StaticAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;

    public PreparedStatementBindVariableInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
    }


    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, className, methodName, parameterDescription, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (!(target instanceof BindValueAccessor)) {
            return;
        }

        final Integer index = NumberUtils.toInteger(args[0]);
        if (index == null) {
            // something is wrong
            return;
        }

        Map<Integer, String> bindList = ((BindValueAccessor) target)._$PINPOINT$_getBindValue();
        if (bindList == null) {
            bindList = new HashMap<Integer, String>();
            ((BindValueAccessor) target)._$PINPOINT$_setBindValue(bindList);
        }

        final String value = BindValueConverter.convert(methodName, args);
        bindList.put(index, value);
    }
}