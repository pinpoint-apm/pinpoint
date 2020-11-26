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
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.EndPointFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.SocketChannelListFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.field.getter.SelectorGetter;

import org.apache.kafka.clients.ClientResponse;
import org.apache.kafka.common.requests.FetchResponse;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class NetworkClientPollInterceptor implements AroundInterceptor {

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
            logger.afterInterceptor(target, args);
        }

        if (!(target instanceof SelectorGetter)) {
            return;
        }
        Object selector = ((SelectorGetter) target)._$PINPOINT$_getSelector();
        if (!(selector instanceof SocketChannelListFieldAccessor)) {
            return;
        }

        if (!(result instanceof List) || CollectionUtils.isEmpty((List) result)) {
            return;
        }

        final String endPointAddress = getEndPointAddressString((SocketChannelListFieldAccessor) selector);

        for (Object object : (List) result) {

            if (!(object instanceof ClientResponse)) {
                continue;
            }
            ClientResponse clientResponse = (ClientResponse) object;
            Object responseBody = clientResponse.responseBody();
            if (!(responseBody instanceof FetchResponse)) {
                continue;
            }

            FetchResponse fetchResponse = (FetchResponse) responseBody;
            Map responseData = fetchResponse.responseData();
            if (responseData == null) {
                continue;
            }

            Set keySet = responseData.keySet();
            for (Object key : keySet) {
                if (key instanceof EndPointFieldAccessor) {
                    EndPointFieldAccessor endPointFieldAccessor = (EndPointFieldAccessor) key;

                    if (endPointFieldAccessor._$PINPOINT$_getEndPoint() == null) {
                        endPointFieldAccessor._$PINPOINT$_setEndPoint(endPointAddress);
                    }
                }

            }
        }
    }


    private String getEndPointAddressString(SocketChannelListFieldAccessor socketChannelListFieldAccessor) {
        List<SocketChannel> socketChannels = socketChannelListFieldAccessor._$PINPOINT$_getSocketChannelList();
        if (CollectionUtils.isEmpty(socketChannels)) {
            return null;
        }

        List<String> endPointAddressList = new ArrayList<String>(socketChannels.size());
        for (SocketChannel socketChannel : socketChannels) {
            try {
                if (!socketChannel.isConnected()) {
                    continue;
                }

                SocketAddress localAddress = socketChannel.getLocalAddress();

                String ipPort = getIpPort(localAddress);
                endPointAddressList.add(ipPort);
            } catch (Exception e) {
            }
        }

        if (endPointAddressList.isEmpty()) {
            return null;
        }

        return endPointAddressList.toString();
    }

    private String getIpPort(SocketAddress socketAddress) {
        String address = socketAddress.toString();

        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            return HostAndPort.toHostAndPortString(inetSocketAddress.getAddress().getHostAddress(), inetSocketAddress.getPort());
        }

        if (address.startsWith("/")) {
            return address.substring(1);
        } else {
            if (address.contains("/")) {
                return address.substring(address.indexOf('/') + 1);
            } else {
                return address;
            }
        }
    }

}
