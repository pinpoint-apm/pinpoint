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

package com.navercorp.pinpoint.plugin.thrift.interceptor.server.nonblocking;

import java.net.Socket;

import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TTransport;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.SocketFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.getter.TNonblockingTransportFieldGetter;

/**
 * Base interceptor for retrieving the socket information from a TTransport field, and injecting it into a transport wrapping it.
 * 
 * @author HyunGil Jeong
 */
public abstract class FrameBufferTransportInjectInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void before(Object target, Object[] args) {
        // Do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (validate0(target, args, result)) {
            if (isDebug) {
                logger.afterInterceptor(target, args, result, throwable);
            }
            Socket rootSocket = getRootSocket(target);
            if (rootSocket != null) {
                TTransport injectionTarget = getInjectionTarget(target, args, result);
                injectSocket(injectionTarget, rootSocket);
            }
        }
    }

    private boolean validate0(Object target, Object[] args, Object result) {
        if (!(target instanceof SocketFieldAccessor)) {
            if (!isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", SocketFieldAccessor.class.getName());
            }
            return false;
        }
        return validate(target, args, result);
    }

    protected boolean validate(Object target, Object[] args, Object result) {
        return true;
    }

    protected abstract TTransport getInjectionTarget(Object target, Object[] args, Object result);

    // Retrieve the socket information from the trans_ field of the given instance.
    protected final Socket getRootSocket(Object target) {
        if (target instanceof TNonblockingTransportFieldGetter) {
            TNonblockingTransport inTrans = ((TNonblockingTransportFieldGetter) target)._$PINPOINT$_getTNonblockingTransport();
            if (inTrans != null) {
                if (inTrans instanceof SocketFieldAccessor) {
                    return ((SocketFieldAccessor) inTrans)._$PINPOINT$_getSocket();
                } else {
                    if (isDebug) {
                        logger.debug("Invalid target object. Need field accessor({}).", SocketFieldAccessor.class.getName());
                    }
                }
            }
        }
        return null;
    }

    // Inject the socket information into the given memory-based transport
    protected final void injectSocket(TTransport inTrans, Socket rootSocket) {
        if (!(inTrans instanceof SocketFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", SocketFieldAccessor.class.getName());
            }
            return;
        }
        ((SocketFieldAccessor)inTrans)._$PINPOINT$_setSocket(rootSocket);
    }
}
