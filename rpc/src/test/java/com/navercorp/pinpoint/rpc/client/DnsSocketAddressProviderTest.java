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

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;


/**
 * @author Woonduk Kang(emeroad)
 */
public class DnsSocketAddressProviderTest {

    @Test
    public void resolve_success() {
        String hostName = "127.0.0.1";
        int port = 80;
        DnsSocketAddressProvider localhost = new DnsSocketAddressProvider(hostName, port);
        Assert.assertEquals(localhost.resolve(), new InetSocketAddress(hostName, port));
        Assert.assertEquals(localhost.resolve(), new InetSocketAddress(hostName, port));

    }


    @Test
    public void resolve_fail() {
        String hostName = "empty";
        int port = 80;
        DnsSocketAddressProvider empty = new DnsSocketAddressProvider(hostName, port);
        Assert.assertEquals(empty.resolve(), new InetSocketAddress(hostName, port));
        Assert.assertEquals(empty.resolve(), new InetSocketAddress(hostName, port));

    }


    /**
     * check npe
     */
    @Test
    public void resolve_update() {
        final String host1 = "127.0.0.1";
        final String host2 = "127.0.0.2";
        int port = 80;
        DnsSocketAddressProvider empty = new DnsSocketAddressProvider("empty", port) {
            private int i = 0;
            @Override
            InetAddress getByName(String host) throws UnknownHostException {
                if (i == 0) {
                    i++;
                    return InetAddress.getByName(host1);
                } else {
                    return InetAddress.getByName(host2);
                }
            }
        };
        Assert.assertEquals(empty.resolve(), new InetSocketAddress(host1, port));
        Assert.assertEquals(empty.resolve(), new InetSocketAddress(host2, port));
    }
}