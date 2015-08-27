/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift.common;

import java.net.InetSocketAddress;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

/**
 * @author HyunGil Jeong
 */
public class TestEnvironment {

    private static final int MIN_SERVER_PORT = 9091;
    private static final int MAX_SERVER_PORT = 9099;

    private static final String SERVER_IP = "127.0.0.1";
    private static final TProtocolFactory PROTOCOL_FACTORY = new TBinaryProtocol.Factory();

    private final String serverIp = SERVER_IP;
    private final int port = MIN_SERVER_PORT + (int)(Math.random() * (MAX_SERVER_PORT - MIN_SERVER_PORT) + 1);
    private final InetSocketAddress serverAddress = new InetSocketAddress(SERVER_IP, this.port);
    private final TProtocolFactory protocolFactory = PROTOCOL_FACTORY;

    public String getServerIp() {
        return this.serverIp;
    }

    public int getPort() {
        return this.port;
    }

    public InetSocketAddress getServerAddress() {
        return this.serverAddress;
    }

    public TProtocolFactory getProtocolFactory() {
        return this.protocolFactory;
    }

}
