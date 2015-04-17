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

import java.sql.Connection;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetMethod;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Targets;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.jdbc.common.JdbcDriverConstants;
import com.navercorp.pinpoint.plugin.jdbc.common.UnKnownDatabaseInfo;

/**
 * @author emeroad
 */
@Targets(methods={
        @TargetMethod(name="createStatement"),
        @TargetMethod(name="createStatement", paramTypes={"int", "int"}),
        @TargetMethod(name="createStatement", paramTypes={"int", "int", "int"})
})
public class StatementCreateInterceptor implements SimpleAroundInterceptor, JdbcDriverConstants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MetadataAccessor databaseInfoAccessor;
    
    public StatementCreateInterceptor(TraceContext traceContext, @Name(DATABASE_INFO) MetadataAccessor databaseInfoAccessor) {
        this.traceContext = traceContext;
        this.databaseInfoAccessor = databaseInfoAccessor;
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

        if (InterceptorUtils.isThrowable(throwable)) {
            return;
        }
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        if (target instanceof Connection) {
            final DatabaseInfo databaseInfo = databaseInfoAccessor.get(target, UnKnownDatabaseInfo.INSTANCE);
            databaseInfoAccessor.set(result, databaseInfo);
        }
    }
}
