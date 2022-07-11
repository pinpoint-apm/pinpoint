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

package com.navercorp.pinpoint.plugin.mongo.interceptor;

import com.mongodb.ServerAddress;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.mongo.HostListAccessor;

import java.util.ArrayList;
import java.util.List;

public class MongoClientConstructorInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public MongoClientConstructorInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (throwable != null) {
            return;
        }

        if (Boolean.FALSE == (target instanceof HostListAccessor)) {
            logger.info("Unexpected target. The target is not a HostListAccessor implementation. target={}", target);
            return;
        }

        final List<String> hostList = new ArrayList<>();
        // arg0 is ServerAddress
        final ServerAddress serverAddress = ArrayArgumentUtils.getArgument(args, 0, ServerAddress.class);
        if (serverAddress != null) {
            final String hostAddress = HostAndPort.toHostAndPortString(serverAddress.getHost(), serverAddress.getPort());
            hostList.add(hostAddress);
        } else {
            // arg0 is List<ServerAddress>
            final List list = ArrayArgumentUtils.getArgument(args, 0, List.class);
            if (list != null) {
                for (Object o : list) {
                    if (o instanceof ServerAddress) {
                        final String hostAddress = HostAndPort.toHostAndPortString(serverAddress.getHost(), serverAddress.getPort());
                        hostList.add(hostAddress);
                    }
                }
            }
        }

        ((HostListAccessor) target)._$PINPOINT$_setHostList(hostList);
        if (isDebug) {
            logger.debug("Set hostList={}", hostList);
        }
    }
}
