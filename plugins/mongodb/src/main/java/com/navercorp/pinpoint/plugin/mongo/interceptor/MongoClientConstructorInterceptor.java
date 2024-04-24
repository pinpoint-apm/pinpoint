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

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.mongo.HostListAccessor;
import com.navercorp.pinpoint.plugin.mongo.MongoUtil;

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
            return;
        }

        try {
            // over 4.2
            final MongoClientSettings mongoClientSettings = ArrayArgumentUtils.getArgument(args, 0, MongoClientSettings.class);
            if (mongoClientSettings != null) {
                List<String> list = MongoUtil.getHostList(mongoClientSettings);
                setHostList(target, list);
                return;
            }

            final List<String> hostList = new ArrayList<>();
            // arg0 is ServerAddress
            final ServerAddress serverAddress = ArrayArgumentUtils.getArgument(args, 0, ServerAddress.class);
            if (serverAddress != null) {
                final String hostAddress = HostAndPort.toHostAndPortString(serverAddress.getHost(), serverAddress.getPort());
                hostList.add(hostAddress);
                setHostList(target, hostList);
                return;
            }

            // arg0 is List<ServerAddress>
            final List<?> list = ArrayArgumentUtils.getArgument(args, 0, List.class);
            if (list != null) {
                for (Object o : list) {
                    if (o instanceof ServerAddress) {
                        // Set multi address.
                        final ServerAddress address = (ServerAddress) o;
                        final String hostAddress = HostAndPort.toHostAndPortString(address.getHost(), address.getPort());
                        hostList.add(hostAddress);
                    }
                }
                setHostList(target, hostList);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private void setHostList(Object target, List<String> hostList) {
        ((HostListAccessor) target)._$PINPOINT$_setHostList(hostList);
        if (isDebug) {
            logger.debug("Set hostList={}", hostList);
        }
    }
}