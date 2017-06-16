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

package com.navercorp.pinpoint.plugin.cassandra.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.cassandra.CassandraConstants;

/**
 * @author dawidmalina
 */
public class CassandraDriverConnectInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final boolean recordConnection;

    public CassandraDriverConnectInterceptor(TraceContext context, MethodDescriptor descriptor) {
        this(context, descriptor, true);
    }

    public CassandraDriverConnectInterceptor(TraceContext context, MethodDescriptor descriptor,
            boolean recordConnection) {
        super(context, descriptor);
        this.recordConnection = recordConnection;
    }

    @Override
    protected void logBeforeInterceptor(Object target, Object[] args) {
        // Must not log args because it contains a password
        logger.beforeInterceptor(target, null);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        logger.afterInterceptor(target, null, result, throwable);
    }

    @Override
    protected void prepareAfterTrace(Object target, Object[] args, Object result, Throwable throwable) {
        final boolean success = InterceptorUtils.isSuccess(throwable);
        // Must not check if current transaction is trace target or not. Connection can be made by other thread.
        final List<String> hostList = getHostList(target);

        if (args == null) {
            return;
        }
        final String keyspace = (String) args[0];
        DatabaseInfo databaseInfo = createDatabaseInfo(keyspace, hostList);
        if (success) {
            if (recordConnection) {
                if (result instanceof DatabaseInfoAccessor) {
                    ((DatabaseInfoAccessor) result)._$PINPOINT$_setDatabaseInfo(databaseInfo);
                }
            }
        }
    }

    private List<String> getHostList(Object target) {
        if (!(target instanceof Cluster)) {
            return Collections.emptyList();
        }

        final Cluster cluster = (Cluster) target;
        final Set<Host> hosts = cluster.getMetadata().getAllHosts();
        final int port = cluster.getConfiguration().getProtocolOptions().getPort();
        final List<String> hostList = new ArrayList<String>();
        for (Host host : hosts) {
            final String hostAddress = HostAndPort.toHostAndPortString(host.getAddress().getHostAddress(), port);
            hostList.add(hostAddress);
        }
        return hostList;
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
            Throwable throwable) {

        if (recordConnection) {
            DatabaseInfo databaseInfo;
            if (result instanceof DatabaseInfoAccessor) {
                databaseInfo = ((DatabaseInfoAccessor) result)._$PINPOINT$_getDatabaseInfo();
            } else {
                databaseInfo = null;
            }

            if (databaseInfo == null) {
                databaseInfo = UnKnownDatabaseInfo.INSTANCE;
            }

            // Count database connect too because it's very heavy operation
            recorder.recordServiceType(databaseInfo.getType());
            recorder.recordEndPoint(databaseInfo.getMultipleHost());
            recorder.recordDestinationId(databaseInfo.getDatabaseId());
        }

        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    private DatabaseInfo createDatabaseInfo(String keyspace, List<String> hostList) {
        if (keyspace == null) {
            return UnKnownDatabaseInfo.INSTANCE;
        }

        DatabaseInfo databaseInfo = new DefaultDatabaseInfo(CassandraConstants.CASSANDRA, CassandraConstants.CASSANDRA_EXECUTE_QUERY,
                null, null, hostList, keyspace);

        if (isDebug) {
            logger.debug("parse DatabaseInfo:{}", databaseInfo);
        }

        return databaseInfo;
    }

}
