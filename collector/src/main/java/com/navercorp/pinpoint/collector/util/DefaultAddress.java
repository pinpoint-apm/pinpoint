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

package com.navercorp.pinpoint.collector.util;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAddress implements Address {
    private final String host;
    private final int port;

    public DefaultAddress(String host, int port) {
        this.host = Objects.requireNonNull(host, "host");
        if (!HostAndPort.isValidPort(port)) {
            throw new IllegalArgumentException("out of range:" + port);
        }
        this.port = port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultAddress that = (DefaultAddress) o;

        if (port != that.port) return false;
        return host != null ? host.equals(that.host) : that.host == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "DefaultAddress{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
