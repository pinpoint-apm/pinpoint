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

package com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper;

import java.net.Socket;

import org.apache.thrift.transport.TTransport;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.SocketFieldAccessor;

/**
 * @author HyunGil Jeong
 */
public abstract class WrappedTTransportConstructInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public final void before(Object target, Object[] args) {
        // Do nothing
    }

    @Override
    public final void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (validateTransport(target)) {
            TTransport wrappedTransport = getWrappedTransport(args);
            if (validateTransport(wrappedTransport)) {
                Socket socket = ((SocketFieldAccessor)wrappedTransport)._$PINPOINT$_getSocket();
                ((SocketFieldAccessor)target)._$PINPOINT$_setSocket(socket);
            }
        }
    }

    protected abstract TTransport getWrappedTransport(Object[] args);

    private boolean validateTransport(Object transport) {
        if (transport instanceof TTransport) {
            return validateTransport((TTransport)transport);
        }
        return false;
    }

    private boolean validateTransport(TTransport transport) {
        if (!(transport instanceof SocketFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", SocketFieldAccessor.class.getName());
            }
            return false;
        }
        return true;
    }

}
