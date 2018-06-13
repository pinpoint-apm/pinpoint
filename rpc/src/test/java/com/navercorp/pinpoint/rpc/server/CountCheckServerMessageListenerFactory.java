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

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Taejin Koo
 */
public class CountCheckServerMessageListenerFactory implements ServerMessageListenerFactory {

    private CountDownLatch sendCountDownLatch;
    private CountDownLatch requestCountDownLatch;
    private CountDownLatch pingCountDownLatch;

    public CountCheckServerMessageListenerFactory() {
    }

    public CountDownLatch getSendCountDownLatch() {
        return sendCountDownLatch;
    }

    public void setSendCountDownLatch(CountDownLatch sendCountDownLatch) {
        this.sendCountDownLatch = sendCountDownLatch;
    }

    public CountDownLatch getRequestCountDownLatch() {
        return requestCountDownLatch;
    }

    public void setRequestCountDownLatch(CountDownLatch requestCountDownLatch) {
        this.requestCountDownLatch = requestCountDownLatch;
    }

    public CountDownLatch getPingCountDownLatch() {
        return pingCountDownLatch;
    }

    public void setPingCountDownLatch(CountDownLatch pingCountDownLatch) {
        this.pingCountDownLatch = pingCountDownLatch;
    }

    @Override
    public ServerMessageListener create() {
        return new PingCheckServerMessageListener(sendCountDownLatch, requestCountDownLatch, pingCountDownLatch);
    }


    private static class PingCheckServerMessageListener implements ServerMessageListener {

        private final CountDownLatch sendCountDownLatch;
        private final CountDownLatch requestCountDownLatch;
        private final CountDownLatch pingCountDownLatch;

        public PingCheckServerMessageListener(CountDownLatch sendCountDownLatch, CountDownLatch requestCountDownLatch, CountDownLatch pingCountDownLatch) {
            this.sendCountDownLatch = sendCountDownLatch;
            this.requestCountDownLatch = requestCountDownLatch;
            this.pingCountDownLatch = pingCountDownLatch;
        }

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            if (sendCountDownLatch != null) {
                sendCountDownLatch.countDown();
            }
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            if (requestCountDownLatch != null) {
                requestCountDownLatch.countDown();
            }
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            return HandshakeResponseCode.DUPLEX_COMMUNICATION;
        }

        @Override
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
            if (pingCountDownLatch != null) {
                pingCountDownLatch.countDown();
            }
        }

    }

}
