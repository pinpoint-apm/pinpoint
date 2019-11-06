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

import java.net.InetSocketAddress;

/**
 * @author Woonduk Kang(emeroad)
 */
@Deprecated
public class StaticSocketAddressProvider implements SocketAddressProvider {
    private final InetSocketAddress socketAddress;

    public StaticSocketAddressProvider(InetSocketAddress inetSocketAddress) {
        this.socketAddress = Assert.requireNonNull(inetSocketAddress, "inetSocketAddress");
    }

    @Override
    public InetSocketAddress resolve() {
        return socketAddress;
    }

    @Override
    public String toString() {
        return "StaticSocketAddressProvider{" +
                "socketAddress=" + socketAddress +
                '}';
    }
}
