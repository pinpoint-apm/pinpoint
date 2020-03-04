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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SocketOption implements Cloneable {

    private static final String CONNECT_TIMEOUT_MILLIS = "connectTimeoutMillis";
    static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private final int connectTimeout;
    private final boolean tcpNoDelay;
    private final boolean keepAlive;

    private final int sendBufferSize;
    private final int receiveBufferSize;

    private final int writeBufferHighWaterMark ;
    private final int writeBufferLowWaterMark;

    private SocketOption(int connectTimeout, boolean tcpNoDelay, boolean keepAlive, int sendBufferSize, int receiveBufferSize, int writeBufferHighWaterMark, int writeBufferLowWaterMark) {
        this.connectTimeout = connectTimeout;
        this.tcpNoDelay = tcpNoDelay;
        this.keepAlive = keepAlive;
        this.sendBufferSize = sendBufferSize;
        this.receiveBufferSize = receiveBufferSize;
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }


    public boolean isKeepAlive() {
        return keepAlive;
    }


    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public Map<String, Object> toMap() {

        final Map<String, Object> options = new HashMap<String, Object>();
        // connectTimeout
        options.put(CONNECT_TIMEOUT_MILLIS, connectTimeout);
        // read write timeout needed?  isn't it needed because of nio?

        // tcp setting
        options.put("tcpNoDelay", tcpNoDelay);
        options.put("keepAlive", keepAlive);
        // buffer setting
        options.put("sendBufferSize", sendBufferSize);
        options.put("receiveBufferSize", receiveBufferSize);

        options.put("writeBufferHighWaterMark", writeBufferHighWaterMark);
        options.put("writeBufferLowWaterMark", writeBufferLowWaterMark);

        return options;
    }

    public static class Builder {

        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private boolean tcpNoDelay = true;
        private boolean keepAlive = true;

        private int sendBufferSize = 1024*64;
        private int receiveBufferSize = 1024 * 64;

        private int writeBufferHighWaterMark = 1024 * 1024 * 16;
        private int writeBufferLowWaterMark = 1024 * 1024 * 8;

        public Builder() {
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            if (connectTimeout < 0) {
                throw new IllegalArgumentException("connectTimeout cannot be a negative number");
            }
            this.connectTimeout = connectTimeout;
        }

        public boolean isTcpNoDelay() {
            return tcpNoDelay;
        }

        public void setTcpNoDelay(boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
        }

        public boolean isKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
        }

        public int getSendBufferSize() {
            return sendBufferSize;
        }

        public void setSendBufferSize(int sendBufferSize) {
            this.sendBufferSize = sendBufferSize;
        }

        public int getReceiveBufferSize() {
            return receiveBufferSize;
        }

        public void setReceiveBufferSize(int receiveBufferSize) {
            this.receiveBufferSize = receiveBufferSize;
        }

        public int getWriteBufferHighWaterMark() {
            return writeBufferHighWaterMark;
        }

        public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
            Assert.isTrue(writeBufferHighWaterMark > 0, "must be writeBufferHighWaterMark > 0");
            this.writeBufferHighWaterMark = (int) writeBufferHighWaterMark;
        }

        public int getWriteBufferLowWaterMark() {
            return writeBufferLowWaterMark;
        }

        public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
            Assert.isTrue(writeBufferLowWaterMark > 0, "must be writeBufferLowWaterMark > 0");
            this.writeBufferLowWaterMark = (int) writeBufferLowWaterMark;
        }

        public SocketOption build() {
            return new SocketOption(this.connectTimeout, this.tcpNoDelay, this.keepAlive, this.sendBufferSize, this.receiveBufferSize, writeBufferHighWaterMark, writeBufferLowWaterMark);
        }
    }
}
