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

package com.navercorp.pinpoint.plugin.jdbc.common.interceptor;

import java.util.Map;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetFilter;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.plugin.jdbc.common.JdbcDriverConstants;
import com.navercorp.pinpoint.plugin.jdbc.common.bindvalue.BindValueConverter;

/**
 * @author emeroad
 */
@TargetFilter(type="com.navercorp.pinpoint.plugin.jdbc.common.PreparedStatementBindingMethodFilter", singleton=true)
public class PreparedStatementBindVariableInterceptor implements StaticAroundInterceptor, JdbcDriverConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MetadataAccessor bindValueAccessor;
    
    public PreparedStatementBindVariableInterceptor(TraceContext traceContext, @Name(BIND_VALUE) MetadataAccessor bindValueAccessor) {
        this.traceContext = traceContext;
        this.bindValueAccessor = bindValueAccessor;
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
        Map<Integer, String> bindList = null;
        if (bindValueAccessor.isApplicable(target)) {
            bindList = bindValueAccessor.get(target);
        }
        if (bindList == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("bindValue is null");
            }
            return;
        }
        Integer index = NumberUtils.toInteger(args[0]);
        if (index == null) {
            // something is wrong
            return;
        }
        String value = BindValueConverter.convert(methodName, args);
        bindList.put(index, value);

    }
}
