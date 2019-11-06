/*
 * Copyright 2018 NAVER Corp.
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
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.List;

public class ConsumerConstructorInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public ConsumerConstructorInterceptor() {
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

        if (throwable != null) {
            return;
        }

        if (!(target instanceof RemoteAddressFieldAccessor)) {
            return;
        }

        ConsumerConfig consumerConfig = getConsumerConfig(args);
        if (consumerConfig == null) {
            return;
        }

        String remoteAddress = getRemoteAddress(consumerConfig);
        ((RemoteAddressFieldAccessor) target)._$PINPOINT$_setRemoteAddress(remoteAddress);
    }

    private ConsumerConfig getConsumerConfig(Object args[]) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        if (args[0] instanceof ConsumerConfig) {
            return (ConsumerConfig)args[0];
        }

        return null;
    }

    private String getRemoteAddress(ConsumerConfig consumerConfig) {
        List<String> serverList = consumerConfig.getList("bootstrap.servers");
        String remoteAddress = KafkaConstants.UNKNOWN;
        if (CollectionUtils.nullSafeSize(serverList) == 1) {
            remoteAddress = serverList.get(0);
        } else if (CollectionUtils.nullSafeSize(serverList) > 1) {
            remoteAddress = serverList.toString();
        }
        return remoteAddress;
    }

}
