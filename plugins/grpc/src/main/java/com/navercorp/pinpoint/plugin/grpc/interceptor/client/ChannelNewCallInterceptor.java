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

package com.navercorp.pinpoint.plugin.grpc.interceptor.client;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.grpc.field.accessor.MethodNameAccessor;
import com.navercorp.pinpoint.plugin.grpc.field.accessor.RemoteAddressAccessor;
import io.grpc.MethodDescriptor;

/**
 * @author Taejin Koo
 */
public class ChannelNewCallInterceptor implements AroundInterceptor {

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

        if (ArrayUtils.isEmpty(args)) {
            return;
        }

        setMethodName(result, args[0]);
        setRemoteAddress(result, target);
    }

    private void setMethodName(Object result, Object methodDescriptor) {
        if (!(result instanceof MethodNameAccessor)) {
            if (isDebug) {
                logger.debug("invalid result object. result:{}", result);
            }
            return;
        }

        try {
            if (methodDescriptor instanceof io.grpc.MethodDescriptor) {
                String fullMethodName = ((MethodDescriptor) methodDescriptor).getFullMethodName();
                ((MethodNameAccessor) result)._$PINPOINT$_setMethodName((String) fullMethodName);
            } else {
                if (isDebug) {
                    logger.debug("invalid methodDescriptor. methodDescriptor:{}", methodDescriptor);
                }
            }
        } catch (Exception e) {
            if (isDebug) {
                logger.debug("failed to invoke getFullMethodName method. caused:{}", e.getMessage(), e);
            }
        }
    }

    private void setRemoteAddress(Object result, Object channel) {
        if (!(result instanceof RemoteAddressAccessor)) {
            if (isDebug) {
                logger.debug("invalid result object. result:{}", result);
            }
            return;
        }

        try {
            if (channel instanceof io.grpc.Channel) {
                String remoteAddress = ((io.grpc.Channel) channel).authority();
                ((RemoteAddressAccessor) result)._$PINPOINT$_setRemoteAddress((String) remoteAddress);
            } else {
                if (isDebug) {
                    logger.debug("invalid channel. channel:{}", channel);
                }
            }
        } catch (Exception e) {
            if (isDebug) {
                logger.debug("failed to invoke authority method. caused:{}", e.getMessage(), e);
            }
        }
    }

}