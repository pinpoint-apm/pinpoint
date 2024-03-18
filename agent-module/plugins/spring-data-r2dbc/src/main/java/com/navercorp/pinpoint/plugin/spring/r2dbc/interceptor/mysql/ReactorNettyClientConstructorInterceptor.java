/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mysql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.SetDatabaseInfoInterceptor;
import io.netty.channel.Channel;
import reactor.netty.Connection;
import reactor.netty.channel.ChannelOperations;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import static com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants.UNKNOWN_DATABASE;

public class ReactorNettyClientConstructorInterceptor extends SetDatabaseInfoInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public DatabaseInfo getDatabaseInfo(Object target, Object[] args, Object result) {
        final Connection connection = ArrayArgumentUtils.getArgument(args, 0, Connection.class);
        if (connection == null) {
            return null;
        }
        if (Boolean.FALSE == (connection instanceof ChannelOperations)) {
            return null;
        }

        final ChannelOperations channelOperations = (ChannelOperations) connection;
        final String host = getRemoteAddress(channelOperations.channel());
        final List<String> hostList = Arrays.asList(host);
        final String database = UNKNOWN_DATABASE;
        final DatabaseInfo databaseInfo = new DefaultDatabaseInfo(SpringDataR2dbcConstants.SPRING_DATA_R2DBC_MYSQL, SpringDataR2dbcConstants.SPRING_DATA_R2DBC_MYSQL_EXECUTE_QUERY, null, null, hostList, database);
        if (isDebug) {
            logger.debug("Create databaseInfo={}", databaseInfo);
        }
        return databaseInfo;
    }

    @Override
    public boolean setDatabaseInfo(DatabaseInfo databaseInfo, Object target, Object[] args, Object result) {
        if (target instanceof DatabaseInfoAccessor) {
            ((DatabaseInfoAccessor) target)._$PINPOINT$_setDatabaseInfo(databaseInfo);
            return true;
        }
        return false;
    }

    String getRemoteAddress(Channel channel) {
        if (channel == null) {
            return "unknown";
        }
        try {
            final InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
            final String host = SocketAddressUtils.getHostNameFirst(socketAddress);
            if (host != null) {
                return HostAndPort.toHostAndPortString(host, socketAddress.getPort());
            }
        } catch (Exception ignored) {
            if (isDebug) {
                logger.debug("Failed to get remote address. channel={}", channel, ignored);
            }
        }
        return "unknown";
    }
}
