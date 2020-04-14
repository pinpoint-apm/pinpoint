/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.collector.util;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class MultipleAddress implements Address {

    private final List<String> hostList;
    private final int port;

    private int index = 0;

    public MultipleAddress(List<String> hostList, int port) {
        this.hostList = Objects.requireNonNull(hostList, "hostList");
        if (!HostAndPort.isValidPort(port)) {
            throw new IllegalArgumentException("out of range:" + port);
        }
        this.port = port;
    }

    @Override
    public String getHost() {
        int addressIndex = index;
        this.index = ++index % hostList.size();
        return hostList.get(addressIndex);
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MultiAddressProvider{");
        sb.append("index=").append(index);
        sb.append(", addresses=").append(hostList);
        sb.append(", port=").append(port);
        sb.append('}');
        return sb.toString();
    }

}
