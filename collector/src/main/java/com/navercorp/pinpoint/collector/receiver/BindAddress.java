/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver;

import java.util.Objects;

/**
 * @author emeroad
 */
public class BindAddress {
    private final String ip;
    private final int port;

    public BindAddress(String ip, int port) {
        this.ip = Objects.requireNonNull(ip, "ip");
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String ip = "0.0.0.0";
        private int port;

        Builder() {
        }

        public Builder setIp(String ip) {
            this.ip = Objects.requireNonNull(ip, "ip");
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public BindAddress build() {
            return new BindAddress(this.ip, this.port);
        }
    }

    @Override
    public String toString() {
        return "BindAddress{" +
                ip + ':' + port +
                '}';
    }
}
