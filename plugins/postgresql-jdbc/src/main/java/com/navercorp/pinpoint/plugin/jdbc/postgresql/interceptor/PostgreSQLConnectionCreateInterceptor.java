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

package com.navercorp.pinpoint.plugin.jdbc.postgresql.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcContext;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.jdbc.postgresql.PostgreSqlConstants;

/**
 * @author Brad Hong
 */
// #1375 Workaround java level Deadlock
// https://oss.navercorp.com/pinpoint/pinpoint-naver/issues/1375
// @TargetConstructor({ "org.postgresql.util.HostSpec[]", "java.lang.String", "java.lang.String", "java.util.Properties", "java.lang.String" })
public class PostgreSQLConnectionCreateInterceptor implements AroundInterceptor {

    private static final String DEFAULT_PORT = "5432";
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;

    public PostgreSQLConnectionCreateInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        if (ArrayUtils.getLength(args) != 5) {
            return;
        }

        final String url = (String) args[4];

        final JdbcContext jdbcContext = traceContext.getJdbcContext();
        jdbcContext.parseJdbcUrl(PostgreSqlConstants.POSTGRESQL, url);
        DatabaseInfo databaseInfo = jdbcContext.parseJdbcUrl(PostgreSqlConstants.POSTGRESQL, url);
        // It's dangerous to use this url directly
//            databaseInfo = createDatabaseInfo(hostToConnectTo, portToConnectTo, databaseId);
        if (InterceptorUtils.isSuccess(throwable)) {
            // Set only if connection is success.
            if (target instanceof DatabaseInfoAccessor) {
                ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
            }
        }
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        // We must do this if current transaction is being recorded.
        if (databaseInfo != null) {
            recorder.recordServiceType(databaseInfo.getType());
            recorder.recordEndPoint(databaseInfo.getMultipleHost());
            recorder.recordDestinationId(databaseInfo.getDatabaseId());
        }

    }

    @Override
    public void before(Object target, Object[] args) {

    }
}
