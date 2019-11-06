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

package com.navercorp.pinpoint.rpc;

import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class UnsupportOperationMessageListener implements MessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final UnsupportOperationMessageListener INSTANCE = new UnsupportOperationMessageListener();

    @Override
    public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Unsupported handleSend method");
        errorMessage.append("packet:").append(sendPacket);
        errorMessage.append(", remote::").append(pinpointSocket.getRemoteAddress());

        throw new UnsupportedOperationException(errorMessage.toString());
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Unsupported handleRequest method");
        errorMessage.append("packet:").append(requestPacket);
        errorMessage.append(", remote::").append(pinpointSocket.getRemoteAddress());

        throw new UnsupportedOperationException(errorMessage.toString());
    }

    public static UnsupportOperationMessageListener getInstance() {
        return INSTANCE;
    }

}

