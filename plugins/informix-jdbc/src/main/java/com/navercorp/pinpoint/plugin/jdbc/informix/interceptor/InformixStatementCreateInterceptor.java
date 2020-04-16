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

package com.navercorp.pinpoint.plugin.jdbc.informix.interceptor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;

import java.util.Arrays;
import java.util.Properties;
import com.informix.util.AdvancedUppercaseProperties;

import com.navercorp.pinpoint.plugin.jdbc.informix.InformixConstants;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.getter.InformixDatabaseNameGetter;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.getter.InformixConnectionInfoGetter;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.getter.Informix_4_50_ConnectionInfoGetter;

/**
 * @author Guillermo Adrian Molina
 */
//@TargetMethods({
//        @TargetMethod(name="createStatement"),
//        @TargetMethod(name="createStatement", paramTypes={"int", "int"}),
//        @TargetMethod(name="createStatement", paramTypes={"int", "int", "int"})
//})
public class InformixStatementCreateInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;

    public InformixStatementCreateInterceptor(TraceContext traceContext) {
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
        final DatabaseInfo databaseInfo = getDatabaseInfo(target);
        if (result instanceof DatabaseInfoAccessor) {
            ((DatabaseInfoAccessor) result)._$PINPOINT$_setDatabaseInfo(databaseInfo);
        }
    }

    private DatabaseInfo getDatabaseInfo(Object target) {
        final String url = getURL(target);
        final String dbName = getDatabaseName(target);

        DatabaseInfo databaseInfo = null;
        if (url != null && dbName != null) {
            databaseInfo = new DefaultDatabaseInfo(InformixConstants.INFORMIX, InformixConstants.INFORMIX_EXECUTE_QUERY, url, url, Arrays.asList(url), dbName);
        }

        if (databaseInfo == null) {
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }
        return databaseInfo;
    }

    private String getDatabaseName(Object target) {
        if (target instanceof InformixDatabaseNameGetter) {
            return ((InformixDatabaseNameGetter) target)._$PINPOINT$_getDatabaseName();
        }
        return null;
    }

    private String getURL(Object target) {
        if (target instanceof InformixConnectionInfoGetter) {
            Properties connInfo = ((InformixConnectionInfoGetter) target)._$PINPOINT$_getConnectionInfo();
            String url = connInfo.getProperty("IFXHOST") + ":" + connInfo.getProperty("PORTNO");
            return url;
        }
        if (target instanceof Informix_4_50_ConnectionInfoGetter) {
            AdvancedUppercaseProperties connInfo = ((Informix_4_50_ConnectionInfoGetter) target)._$PINPOINT$_getConnectionInfo();
            String url = connInfo.getProperty("IFXHOST") + ":" + connInfo.getProperty("PORTNO");
            return url;
        }
        return null;
    }
}
