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

package com.navercorp.pinpoint.it.plugin.thrift.common;

import com.navercorp.pinpoint.testcase.util.SocketUtils;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.net.InetSocketAddress;

/**
 * @author HyunGil Jeong
 */
public class TestEnvironment {

    private static final int MIN_SERVER_PORT = 9091;
    private static final int MAX_SERVER_PORT = 9099;

    private static final String SERVER_HOST = "localhost";
    private static final String SERVER_IP = "127.0.0.1";
    private static final TProtocolFactory PROTOCOL_FACTORY = new TBinaryProtocol.Factory();
    private static final String HTTP_PATH = "/thrift";

    private final String serverHost = SERVER_HOST;
    private final String serverIp = SERVER_IP;
    private final int port = SocketUtils.findAvailableTcpPort(10000, 19999);
    private final String httpPath = HTTP_PATH;
    private final String httpUrl = "http://" + serverHost + ":" + port + httpPath;
    private final InetSocketAddress serverAddress = new InetSocketAddress(SERVER_IP, this.port);
    private final TProtocolFactory protocolFactory = PROTOCOL_FACTORY;

    public String getServerHost() {
        return this.serverHost;
    }

    public String getServerIp() {
        return this.serverIp;
    }

    public int getPort() {
        return this.port;
    }

    public String getHttpPath() {
        return this.httpPath;
    }

    public String getHttpUrl() {
        return this.httpUrl;
    }

    public InetSocketAddress getServerAddress() {
        return this.serverAddress;
    }

    public TProtocolFactory getProtocolFactory() {
        return this.protocolFactory;
    }

}
