/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.paho.mqtt.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.BrokerUriFieldAccessor;

import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.UNKNOWN;

/**
 * @author Younsung Hwang
 */
public class MqttClientConstructorInterceptor implements AroundInterceptor {

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

        if (!(target instanceof BrokerUriFieldAccessor)) {
            return;
        }

        String brokerUri = extractBrokerUri(args);
        if (brokerUri != null) {
            ((BrokerUriFieldAccessor) target)._$PINPOINT$_setBrokerUri(brokerUri);
            return;
        }

    }

    private String extractBrokerUri(Object args[]) {
        if (ArrayUtils.isEmpty(args)) {
            return UNKNOWN;
        }

        if (args[0] instanceof String && StringUtils.hasLength((String)args[0])) {
            return (String) args[0];
        }

        return UNKNOWN;
    }
}
