/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.pulsar.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.pulsar.field.accessor.ClientConfigAccessor;
import com.navercorp.pinpoint.plugin.pulsar.field.getter.ServiceUrlGetter;

/**
 * Passes the client configuration along the constructors:
 * PulsarClientImpl(ClientConfigurationData, ...), ProducerBase(PulsarClientImpl, ...) and
 * TypedMessageBuilderImpl(ProducerBase, ...). ConsumerImpl(PulsarClientImpl, ...) does the same in
 * {@link ConsumerConstructorInterceptor}, because a method takes a single interceptor.
 */
public class ClientConfigConstructorInterceptor implements AroundInterceptor {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(final Object target, final Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
    }

    @Override
    public void after(final Object target, final Object[] args, final Object result, final Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        if (throwable != null) {
            return;
        }
        copyClientConfig(target, ArrayArgumentUtils.getArgument(args, 0, Object.class));
    }

    /**
     * @param source ClientConfigurationData, or an object that already holds it
     */
    static void copyClientConfig(final Object target, final Object source) {
        if (!(target instanceof ClientConfigAccessor)) {
            return;
        }
        Object clientConfig = null;
        if (source instanceof ServiceUrlGetter) {
            // ClientConfigurationData itself
            clientConfig = source;
        } else if (source instanceof ClientConfigAccessor) {
            clientConfig = ((ClientConfigAccessor) source)._$PINPOINT$_getClientConfig();
        }
        if (clientConfig != null) {
            ((ClientConfigAccessor) target)._$PINPOINT$_setClientConfig(clientConfig);
        }
    }
}
