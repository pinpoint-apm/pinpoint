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

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.util.Address;
import com.navercorp.pinpoint.rpc.client.SocketAddressProvider;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ClusterAddressProvider implements SocketAddressProvider {

    private final Address address;

    public ClusterAddressProvider(Address address) {
        this.address = Objects.requireNonNull(address, "address");
    }

    @Override
    public InetSocketAddress resolve() {
        String host = address.getHost();
        int port = address.getPort();

        return new InetSocketAddress(host, port);
    }

    @Override
    public String toString() {
        return "ClusterAddressProvider{" + "address=" + address + '}';
    }
}
