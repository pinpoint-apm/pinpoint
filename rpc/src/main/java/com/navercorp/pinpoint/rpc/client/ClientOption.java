/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClientOption {

    // it's better to be a long value. even though keeping ping period from client to server short,
    // disconnection between them dose not be detected quickly.
    // rather keeping it from server to client short help detect disconnection as soon as possible.
    static final long DEFAULT_PING_DELAY = 60 * 1000 * 5;
    static final long DEFAULT_ENABLE_WORKER_PACKET_DELAY = 60 * 1000 * 1;
    static final long DEFAULT_WRITE_TIMEOUT_MILLIS = 3 * 1000;
    static final long DEFAULT_REQUEST_TIMEOUT_MILLIS = 3 * 1000;

    private final long reconnectDelay;
    private final long pingDelay;
    private final long enableWorkerPacketDelay;
    private final long writeTimeoutMillis;
    private final long requestTimeoutMillis;

    private ClientOption(long reconnectDelay, long pingDelay, long enableWorkerPacketDelay, long writeTimeoutMillis, long requestTimeoutMillis) {
        this.reconnectDelay = reconnectDelay;
        this.pingDelay = pingDelay;
        this.enableWorkerPacketDelay = enableWorkerPacketDelay;
        this.writeTimeoutMillis = writeTimeoutMillis;
        this.requestTimeoutMillis = requestTimeoutMillis;
    }

    public long getReconnectDelay() {
        return reconnectDelay;
    }

    public long getPingDelay() {
        return pingDelay;
    }

    public long getEnableWorkerPacketDelay() {
        return enableWorkerPacketDelay;
    }

    public long getWriteTimeoutMillis() {
        return writeTimeoutMillis;
    }

    public long getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }

    @Override
    public String toString() {
        return "ClientOption{" +
                "reconnectDelay=" + reconnectDelay +
                ", pingDelay=" + pingDelay +
                ", enableWorkerPacketDelay=" + enableWorkerPacketDelay +
                ", writeTimeoutMillis=" + writeTimeoutMillis +
                ", requestTimeoutMillis=" + requestTimeoutMillis +
                '}';
    }

    public static class Builder {
        private long reconnectDelay = 3 * 1000;
        private long pingDelay = DEFAULT_PING_DELAY;
        private long enableWorkerPacketDelay = DEFAULT_ENABLE_WORKER_PACKET_DELAY;
        private long writeTimeoutMillis = DEFAULT_WRITE_TIMEOUT_MILLIS;
        private long requestTimeoutMillis = DEFAULT_REQUEST_TIMEOUT_MILLIS;

        public long getReconnectDelay() {
            return reconnectDelay;
        }

        public void setReconnectDelay(long reconnectDelay) {
            if (reconnectDelay < 0) {
                throw new IllegalArgumentException("reconnectDelay cannot be a negative number");
            }
            this.reconnectDelay = reconnectDelay;
        }

        public long getPingDelay() {
            return pingDelay;
        }

        public void setPingDelay(long pingDelay) {
            if (pingDelay < 0) {
                throw new IllegalArgumentException("pingDelay cannot be a negative number");
            }
            this.pingDelay = pingDelay;
        }

        public long getEnableWorkerPacketDelay() {
            return enableWorkerPacketDelay;
        }

        public void setEnableWorkerPacketDelay(long enableWorkerPacketDelay) {
            this.enableWorkerPacketDelay = enableWorkerPacketDelay;
        }

        public long getWriteTimeoutMillis() {
            return writeTimeoutMillis;
        }

        public void setWriteTimeoutMillis(long writeTimeoutMillis) {
            Assert.isTrue(writeTimeoutMillis >= 0, "writeTimeoutMillis cannot be a negative number");
            this.writeTimeoutMillis = writeTimeoutMillis;
        }

        public long getRequestTimeoutMillis() {
            return requestTimeoutMillis;
        }

        public void setRequestTimeoutMillis(long requestTimeoutMillis) {
            Assert.isTrue(requestTimeoutMillis >= 0, "requestTimeoutMillis cannot be a negative number");
            this.requestTimeoutMillis = requestTimeoutMillis;
        }

        public ClientOption build() {
            return new ClientOption(this.reconnectDelay, pingDelay, enableWorkerPacketDelay, writeTimeoutMillis, requestTimeoutMillis);
        }
    }

}
