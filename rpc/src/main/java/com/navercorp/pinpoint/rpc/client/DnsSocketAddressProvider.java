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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DnsSocketAddressProvider implements SocketAddressProvider{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String host;
    private final int port;

    private InetSocketAddress oldAddress;

    public DnsSocketAddressProvider(String host, int port) {
        this.host = Assert.requireNonNull(host, "host");
        this.port = checkPort(port);
    }

    private static int checkPort(int port) {
        if (!HostAndPort.isValidPort(port)) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        return port;
    }


    @Override
    public InetSocketAddress resolve() {
        try {
            InetAddress inetAddress = getByName(host);
            InetSocketAddress updateAddress = new InetSocketAddress(inetAddress, port);

            checkDnsUpdate(updateAddress);

            return updateAddress;
        } catch (UnknownHostException e) {
            logger.info("dns lookup fail. host:{}", host);
            // expected UnknownHostException from tcp connect timing
            return InetSocketAddress.createUnresolved(host, port);
            // or return null;
        }
    }

    @VisibleForTesting
    InetAddress getByName(String host) throws UnknownHostException {
        return InetAddress.getByName(host);
    }

    private void checkDnsUpdate(InetSocketAddress updateAddress) {
        synchronized(this) {
            final InetSocketAddress oldAddress = this.oldAddress;
            if (oldAddress != null) {
                if (!oldAddress.equals(updateAddress)) {
                    logger.info("host address updated, host:{} old:{}, update:{}", host, oldAddress, updateAddress);
                }
            }
            this.oldAddress = updateAddress;
        }
    }

    @Override
    public String toString() {
        return "DnsSocketAddressProvider{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
