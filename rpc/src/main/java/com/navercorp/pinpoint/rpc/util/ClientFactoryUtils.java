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

import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author Taejin Koo
 */
public final class ClientFactoryUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactoryUtils.class);

    public static PinpointClient createPinpointClient(String host, int port, PinpointClientFactory clientFactory) {
        InetSocketAddress connectAddress = new InetSocketAddress(host, port);
        return createPinpointClient(connectAddress, clientFactory);
    }

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
