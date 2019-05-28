/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.util;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author Taejin Koo
 */
public final class ClientFactoryUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactoryUtils.class);


    public interface PinpointClientProvider {
        PinpointClient get();

        String getAddressAsString();
    }

    public static PinpointClientProvider newPinpointClientProvider(String host, int port, PinpointClientFactory clientFactory) {
        return new DnsPinpointClientProvider(host, port, clientFactory);
    }

    private static class DnsPinpointClientProvider implements PinpointClientProvider {
        private final PinpointClientFactory clientFactory;
        private final String host;
        private final int port;

        public DnsPinpointClientProvider(String host, int port, PinpointClientFactory clientFactory) {
            this.host = Assert.requireNonNull(host, "host must not be null");
            this.port = port;
            this.clientFactory = Assert.requireNonNull(clientFactory, "clientFactory must not be null");
        }

        @Override
        public String getAddressAsString() {
            return host + ":" + port;
        }

        @Override
        public PinpointClient get() {
            return createPinpointClient(host, port, clientFactory);
        }
    }

    public static PinpointClient createPinpointClient(String host, int port, PinpointClientFactory clientFactory) {

        PinpointClient pinpointClient = null;
        for (int i = 0; i < 3; i++) {
            try {
                pinpointClient = clientFactory.connect(host, port);
                LOGGER.info("tcp connect success. remote:{}/{}", host, port);
                return pinpointClient;
            } catch (PinpointSocketException e) {
                LOGGER.warn("tcp connect fail. remote:{}/{} try reconnect, retryCount:{}", host, port, i);
            }
        }
        LOGGER.warn("change background tcp connect mode remote:{}/{} ", host, port);
        pinpointClient = clientFactory.scheduledConnect(host, port);

        return pinpointClient;
    }

    @Deprecated
    public static PinpointClientProvider newPinpointClientProvider(InetSocketAddress inetSocketAddress, PinpointClientFactory clientFactory) {
        return new StaticPinpointClientProvider(inetSocketAddress, clientFactory);
    }

    @Deprecated
    private static class StaticPinpointClientProvider implements PinpointClientProvider {
        private final InetSocketAddress inetSocketAddress;
        private final PinpointClientFactory clientFactory;

        public StaticPinpointClientProvider(InetSocketAddress inetSocketAddress, PinpointClientFactory clientFactory) {
            this.inetSocketAddress = Assert.requireNonNull(inetSocketAddress, "host must not be null");
            this.clientFactory = Assert.requireNonNull(clientFactory, "clientFactory must not be null");
        }

        @Override
        public String getAddressAsString() {
            InetAddress address = inetSocketAddress.getAddress();
            if (address != null) {
                return address.getHostAddress() + ":" + inetSocketAddress.getPort();
            } else {
                return "unknown:-1";
            }
        }

        @Override
        public PinpointClient get() {
            return createPinpointClient(inetSocketAddress, clientFactory);
        }
    }

    /**
     * @deprecated Since 1.7.2 Use {@link #createPinpointClient(String, int, PinpointClientFactory)}
     */
    @Deprecated
    public static PinpointClient createPinpointClient(InetSocketAddress connectAddress, PinpointClientFactory clientFactory) {
        PinpointClient pinpointClient = null;
        for (int i = 0; i < 3; i++) {
            try {
                pinpointClient = clientFactory.connect(connectAddress);
                LOGGER.info("tcp connect success. remote:{}", connectAddress);
                return pinpointClient;
            } catch (PinpointSocketException e) {
                LOGGER.warn("tcp connect fail. remote:{} try reconnect, retryCount:{}", connectAddress, i);
            }
        }
        LOGGER.warn("change background tcp connect mode remote:{} ", connectAddress);
        pinpointClient = clientFactory.scheduledConnect(connectAddress);

        return pinpointClient;
    }

}
