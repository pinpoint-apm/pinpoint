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

package com.navercorp.pinpoint.plugin.thrift.interceptor.transport;

import java.net.Socket;
import java.net.SocketAddress;

import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import org.apache.thrift.transport.TNonblockingSocket;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.SocketAddressFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.SocketFieldAccessor;

/**
 * @author HyunGil Jeong
 */
public class TNonblockingSocketConstructInterceptor implements AroundInterceptor {

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
            final Socket socket = ((TNonblockingSocket) target).getSocketChannel().socket();
            ((SocketFieldAccessor) target)._$PINPOINT$_setSocket(socket);

            SocketAddress socketAddress = ArrayArgumentUtils.getArgument(args, 2, SocketAddress.class);
            if (socketAddress == null) {
                socketAddress = ArrayArgumentUtils.getArgument(args, 3, SocketAddress.class);
            }
            if (socketAddress == null) {
                logger.info("Unexpected arguments. arg[2] or arg[3] is a SocketAddress class. args={}", args);
                return;
            }
            ((SocketAddressFieldAccessor) target)._$PINPOINT$_setSocketAddress(socketAddress);
        }
    }

    private boolean validate(Object target) {
        if (!(target instanceof TNonblockingSocket)) {
            return false;
        }
        if (!(target instanceof SocketFieldAccessor)) {
            logger.info("Invalid target object. Need field accessor({}).", SocketFieldAccessor.class.getName());
            return false;
        }
        if (!(target instanceof SocketAddressFieldAccessor)) {
            logger.info("Invalid target object. Need field accessor({}).", SocketAddressFieldAccessor.class.getName());
            return false;
        }
        return true;
    }
}
