/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.util;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.SocketAddressProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
            this.host = Assert.requireNonNull(host, "host");
            this.port = port;
            this.clientFactory = Assert.requireNonNull(clientFactory, "clientFactory");
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

    public static PinpointClient createPinpointClient(SocketAddressProvider addressProvider, PinpointClientFactory clientFactory) {
        PinpointClient pinpointClient = null;
        for (int i = 0; i < 3; i++) {
            try {
                pinpointClient = clientFactory.connect(addressProvider);

                LOGGER.info("tcp connect success. remote:{}", pinpointClient.getRemoteAddress());
                return pinpointClient;
            } catch (PinpointSocketException e) {
                LOGGER.warn("tcp connect fail. remote:{} try reconnect, retryCount:{}", addressProvider, i);
            }
        }
        LOGGER.warn("change background tcp connect mode remote:{} ", addressProvider);
        pinpointClient = clientFactory.scheduledConnect(addressProvider);

        return pinpointClient;
    }

}
