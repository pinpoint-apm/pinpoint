/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.server;

import java.net.SocketAddress;
import java.util.Map;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;

/**
 * @author Taejin Koo
 */
public interface WritablePinpointServer {

    void send(byte[] payload);

    Future request(byte[] payload);
    
    void response(RequestPacket requestPacket, byte[] payload);
    void response(int requestId, byte[] payload);

    ClientStreamChannelContext createStream(byte[] payload, ClientStreamChannelMessageListener clientStreamChannelMessageListener);

    SocketAddress getRemoteAddress();

    Map<Object, Object> getChannelProperties();

}
