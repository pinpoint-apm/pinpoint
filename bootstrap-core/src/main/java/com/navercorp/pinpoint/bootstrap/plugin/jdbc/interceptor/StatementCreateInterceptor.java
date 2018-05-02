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

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.SqlModule;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;

/**
 * @author emeroad
 */
// #1375 Workaround java level Deadlock
// https://oss.navercorp.com/pinpoint/pinpoint-naver/issues/1375
//@TargetMethods({
//        @TargetMethod(name="createStatement"),
//        @TargetMethod(name="createStatement", paramTypes={"int", "int"}),
//        @TargetMethod(name="createStatement", paramTypes={"int", "int", "int"})
//})
public class StatementCreateInterceptor implements AroundInterceptor {

    private static final Class<?> CONNECTION_CLASS = getConnectionClass();

    private static Class<?> getConnectionClass() {
        if (!SqlModule.isSqlModuleEnable()) {
            // If SqlModule does not exist in java9, StatementCreateInterceptor class should not be initialized.
            throw new IllegalStateException("java.sql.Connection not exist");
        }
        return SqlModule.getSqlConnection();
    }

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    
    public StatementCreateInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
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
        if (CONNECTION_CLASS.isInstance(target)) {
            final DatabaseInfo databaseInfo = getDatabaseInfo(target);
            if (result instanceof DatabaseInfoAccessor) {
                ((DatabaseInfoAccessor) result)._$PINPOINT$_setDatabaseInfo(databaseInfo);
            }
        }
    }

    private DatabaseInfo getDatabaseInfo(Object target) {
        DatabaseInfo databaseInfo = null;
        if (target instanceof DatabaseInfoAccessor) {
            databaseInfo = ((DatabaseInfoAccessor) target)._$PINPOINT$_getDatabaseInfo();
        }
        if (databaseInfo == null) {
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }
        return databaseInfo;
    }
}
