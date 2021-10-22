/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;

import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ConsumerConstructor_V_2_7_Interceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

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

        if (throwable != null) {
            return;
        }

        if (!(target instanceof RemoteAddressFieldAccessor)) {
            return;
        }

        Map consumerConfig = getMap(args);
        if (consumerConfig == null) {
            return;
        }

        String remoteAddress = getRemoteAddress(consumerConfig);
        ((RemoteAddressFieldAccessor) target)._$PINPOINT$_setRemoteAddress(remoteAddress);
    }

    private Map getMap(Object args[]) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        if (args[0] instanceof Map) {
            return (Map)args[0];
        }

        return null;
    }

    private String getRemoteAddress(Map map) {
        Object bootstrapServersValue = map.get(KafkaConstants.CONFIG_BOOTSTRAP_SERVERS_KEY);

        if (bootstrapServersValue instanceof String) {
            return (String) bootstrapServersValue;
        }

        if (bootstrapServersValue instanceof List) {
            List bootstrapServerList = (List) bootstrapServersValue;

            if (CollectionUtils.nullSafeSize(bootstrapServerList) == 1 && (bootstrapServerList.get(0) instanceof String)) {
                return (String) bootstrapServerList.get(0);
            } else if (CollectionUtils.nullSafeSize(bootstrapServerList) > 1) {
                for (Object bootstrapServer : bootstrapServerList) {
                    if (!(bootstrapServer instanceof String)) {
                        return KafkaConstants.UNKNOWN;
                    }
                }
                return bootstrapServerList.toString();
            }
        }

        return KafkaConstants.UNKNOWN;
    }

}
