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

package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientConstants;
import com.navercorp.pinpoint.plugin.rabbitmq.client.field.accessor.LocalAddressAccessor;
import com.navercorp.pinpoint.plugin.rabbitmq.client.field.accessor.RemoteAddressAccessor;

import java.net.Socket;

/**
 * @author HyunGil Jeong
 */
public class SocketFrameHandlerConstructInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void before(Object target, Object[] args) {
        // do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!validate(target, args)) {
            return;
        }
        String localAddress = RabbitMQClientConstants.UNKNOWN;
        String remoteAddress = RabbitMQClientConstants.UNKNOWN;
        if (args[0] instanceof Socket) {
            Socket socket = (Socket) args[0];
            localAddress = HostAndPort.toHostAndPortString(socket.getLocalAddress().getHostAddress(), socket.getLocalPort());
            remoteAddress = HostAndPort.toHostAndPortString(socket.getInetAddress().getHostAddress(), socket.getPort());
        }
        ((LocalAddressAccessor) target)._$PINPOINT$_setLocalAddress(localAddress);
        ((RemoteAddressAccessor) target)._$PINPOINT$_setRemoteAddress(remoteAddress);
    }

    private boolean validate(Object target, Object[] args) {
        if (args == null || args.length < 1) {
            return false;
        }
        if (!(target instanceof LocalAddressAccessor)) {
            logger.debug("Invalid target object. Need field accessor({})", LocalAddressAccessor.class.getName());
            return false;
        }
        if (!(target instanceof RemoteAddressAccessor)) {
            logger.debug("Invalid target object. Need field accessor({})", RemoteAddressAccessor.class.getName());
            return false;
        }
        return true;
    }
}
