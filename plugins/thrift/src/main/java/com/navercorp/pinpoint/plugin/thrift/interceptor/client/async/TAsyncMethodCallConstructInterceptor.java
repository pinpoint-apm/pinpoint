/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift.interceptor.client.async;

import java.net.SocketAddress;

import org.apache.thrift.transport.TNonblockingTransport;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.SocketAddressFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.getter.TNonblockingTransportFieldGetter;

/**
 * @author HyunGil Jeong
 */
public class TAsyncMethodCallConstructInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        // Do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        if (validate(target)) {
            TNonblockingTransport transport = ((TNonblockingTransportFieldGetter)target)._$PINPOINT$_getTNonblockingTransport();
            if (validateTransport(transport)) {
                SocketAddress socketAddress = ((SocketAddressFieldAccessor)transport)._$PINPOINT$_getSocketAddress();
                ((SocketAddressFieldAccessor)target)._$PINPOINT$_setSocketAddress(socketAddress);
            }
        }
    }

    private boolean validate(Object target) {
        if (!(target instanceof TNonblockingTransportFieldGetter)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", TNonblockingTransportFieldGetter.class.getName());
            }
            return false;
        }
        if (!(target instanceof SocketAddressFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", SocketAddressFieldAccessor.class.getName());
            }
            return false;
        }
        return true;
    }

    private boolean validateTransport(Object nonblockingTransportObj) {
        if (!(nonblockingTransportObj instanceof SocketAddressFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", SocketAddressFieldAccessor.class.getName());
            }
            return false;
        }
        return true;
    }

}
