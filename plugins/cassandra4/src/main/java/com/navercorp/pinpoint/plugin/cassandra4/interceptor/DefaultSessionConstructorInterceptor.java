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

import com.datastax.oss.driver.api.core.metadata.EndPoint;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.cassandra4.HostListAccessor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DefaultSessionConstructorInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return;
        }

        if (Boolean.FALSE == (target instanceof HostListAccessor)) {
            return;
        }

        try {
            final Set<?> endPointSet = ArrayArgumentUtils.getArgument(args, 1, Set.class);
            if (endPointSet == null) {
                return;
            }

            final List<String> hostList = new ArrayList<>();
            for (Object o : endPointSet) {
                if (o instanceof EndPoint) {
                    final EndPoint endPoint = (EndPoint) o;
                    final String host = toHost(endPoint.resolve());
                    if (StringUtils.hasLength(host)) {
                        hostList.add(host);
                    }
                }
            }
            if (hostList.isEmpty()) {
                hostList.add("UNKNOWN");
            }
            ((HostListAccessor) target)._$PINPOINT$_setHostList(hostList);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private String toHost(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            final InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            final String hostName = SocketAddressUtils.getHostNameFirst(inetSocketAddress);
            if (hostName == null) {
                return "UNKNOWN";
            }
            return HostAndPort.toHostAndPortString(hostName, inetSocketAddress.getPort());
        }
        return "UNKNOWN";
    }
}
