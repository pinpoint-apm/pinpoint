/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.jdbc.mysql.MySqlConstants;
import com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.getter.DatabaseGetter;
import com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.getter.OrigHostToConnectToGetter;
import com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.getter.OrigPortToConnectToGetter;

import java.util.Arrays;

/**
 * @author HyunGil Jeong
 */
// #1375 Workaround java level Deadlock
// https://oss.navercorp.com/pinpoint/pinpoint-naver/issues/1375
// @TargetConstructor({ "com.mysql.cj.core.ConnectionString", "java.lang.String", "int", "java.util.Properties" })
public class MySQL_6_X_ConnectionCreateInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;

    public MySQL_6_X_ConnectionCreateInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final String hostToConnectTo = getHostToConnectTo(target);
        final Integer portToConnectTo = getPortToConnectTo(target);
        final String databaseId = getDatabase(target);
        // In case of loadbalance, connectUrl is modified.
        // final String url = getString(args[4]);
        DatabaseInfo databaseInfo = null;
        if (hostToConnectTo != null && portToConnectTo != null && databaseId != null) {
            // It's dangerous to use this url directly
            databaseInfo = createDatabaseInfo(hostToConnectTo, portToConnectTo, databaseId);
            if (InterceptorUtils.isSuccess(throwable)) {
                // Set only if connection is success.
                if (target instanceof DatabaseInfoAccessor) {
                    ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
                }
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

    private String getHostToConnectTo(Object target) {
        if (target instanceof OrigHostToConnectToGetter) {
            return ((OrigHostToConnectToGetter) target)._$PINPOINT$_getOrigHostToConnectTo();
        }
        return null;
    }

    private Integer getPortToConnectTo(Object target) {
        if (target instanceof OrigPortToConnectToGetter) {
            return ((OrigPortToConnectToGetter) target)._$PINPOINT$_getOrigPortToConnectTo();
        }
        return null;
    }

    private String getDatabase(Object target) {
        if (target instanceof DatabaseGetter) {
            return ((DatabaseGetter) target)._$PINPOINT$_getDatabase();
        }
        return null;
    }
    
    private DatabaseInfo createDatabaseInfo(String url, Integer port, String databaseId) {
        if (url.indexOf(':') == -1) {
            url += ":" + port;
        }

        DatabaseInfo databaseInfo = new DefaultDatabaseInfo(MySqlConstants.MYSQL, MySqlConstants.MYSQL_EXECUTE_QUERY, url, url, Arrays.asList(url), databaseId);
        return databaseInfo;

    }

    private String getString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    private Integer getInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return null;
    }

    @Override
    public void before(Object target, Object[] args) {

    }
}
