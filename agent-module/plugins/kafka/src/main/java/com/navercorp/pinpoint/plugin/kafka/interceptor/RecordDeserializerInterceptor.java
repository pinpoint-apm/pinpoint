/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.EndPointFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;

public class RecordDeserializerInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
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

        // remoteAddress
        if (result instanceof RemoteAddressFieldAccessor) {
            final RemoteAddressFieldAccessor remoteAddressFieldAccessor = ArrayArgumentUtils.getArgument(args, 1, RemoteAddressFieldAccessor.class);
            if (remoteAddressFieldAccessor != null) {
                ((RemoteAddressFieldAccessor) result)._$PINPOINT$_setRemoteAddress(remoteAddressFieldAccessor._$PINPOINT$_getRemoteAddress());
            }
        }
        // endPoint
        if (result instanceof EndPointFieldAccessor) {
            final EndPointFieldAccessor endPointFieldAccessor = ArrayArgumentUtils.getArgument(args, 1, EndPointFieldAccessor.class);
            if (endPointFieldAccessor != null) {
                ((EndPointFieldAccessor) result)._$PINPOINT$_setEndPoint(endPointFieldAccessor._$PINPOINT$_getEndPoint());
            }
        }
    }
}
