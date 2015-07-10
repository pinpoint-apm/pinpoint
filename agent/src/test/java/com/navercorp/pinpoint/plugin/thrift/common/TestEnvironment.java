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
public interface TestEnvironment {

    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT = 9090;
    public static final InetSocketAddress SERVER_ADDRESS = new InetSocketAddress(SERVER_IP, SERVER_PORT);

    public static final TProtocolFactory PROTOCOL_FACTORY = new TBinaryProtocol.Factory();
    
}
