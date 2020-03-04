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

import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ProducerConstructorInterceptor implements AroundInterceptor {

    private static final String KEY_BOOTSTRAP_SERVERS = "bootstrap.servers";

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

        ProducerConfig producerConfig = getProducerConfig(args);
        if (producerConfig != null) {
            String remoteAddress = getRemoteAddress(producerConfig);
            ((RemoteAddressFieldAccessor) target)._$PINPOINT$_setRemoteAddress(remoteAddress);
            return;
        }

        // Version 2.2.0+ is supported.
        Map map = getMap(args);
        if (map != null) {
            Object remoteAddressObject = map.get(KEY_BOOTSTRAP_SERVERS);
            String remoteAddress = getRemoteAddress0(remoteAddressObject);
            ((RemoteAddressFieldAccessor) target)._$PINPOINT$_setRemoteAddress(remoteAddress);
            return;
        }

    }

    private ProducerConfig getProducerConfig(Object args[]) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        if (args[0] instanceof ProducerConfig) {
            return (ProducerConfig) args[0];
        }

        return null;
    }

    private Map getMap(Object args[]) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        if (args[0] instanceof Map) {
            return (Map) args[0];
        }

        return null;
    }

    private String getRemoteAddress(ProducerConfig producerConfig) {
        List<String> serverList = producerConfig.getList(KEY_BOOTSTRAP_SERVERS);
        return getRemoteAddress0(serverList);
    }

    private String getRemoteAddress0(Object remoteAddressObject) {
        String remoteAddress = KafkaConstants.UNKNOWN;
        if (remoteAddressObject instanceof List) {
            List remoteAddressList = (List) remoteAddressObject;
            if (CollectionUtils.nullSafeSize(remoteAddressList) == 1) {
                remoteAddress = String.valueOf(remoteAddressList.get(0));
            } else if (CollectionUtils.nullSafeSize(remoteAddressList) > 1) {
                remoteAddress = remoteAddressList.toString();
            }
            return remoteAddress;
        } else if (remoteAddressObject instanceof String) {
            return (String) remoteAddressObject;
        }

        return remoteAddress;
    }

}
