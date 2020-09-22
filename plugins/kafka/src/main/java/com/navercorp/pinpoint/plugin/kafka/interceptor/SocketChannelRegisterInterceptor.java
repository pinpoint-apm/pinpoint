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

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.SocketChannelListFieldAccessor;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class SocketChannelRegisterInterceptor implements AroundInterceptor {

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

        if (ArrayUtils.getLength(args) != 3) {
            return;
        }

        final SocketChannel socketChannel = getSocketChannel(args);
        if (socketChannel != null) {
            if (target instanceof SocketChannelListFieldAccessor) {
                List<SocketChannel> socketChannels = ((SocketChannelListFieldAccessor) target)._$PINPOINT$_getSocketChannelList();

                if (socketChannels == null) {
                    socketChannels = new ArrayList<SocketChannel>();
                    ((SocketChannelListFieldAccessor) target)._$PINPOINT$_setSocketAddress(socketChannels);
                }

                socketChannels.add(socketChannel);
            }
        }
    }

    private SocketChannel getSocketChannel(Object[] args) {

        if (args[1] instanceof SocketChannel) {
            return (SocketChannel) args[1];
        }

        if (args[0] instanceof SocketChannel) {
            return (SocketChannel) args[0];
        }

        return null;
    }

}
