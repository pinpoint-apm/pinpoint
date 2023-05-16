/*
 * Copyright 2019 NAVER Corp.
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


package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;

import java.net.SocketAddress;

/**
 * @author Taejin Koo
 */
public interface StreamChannel {

    SocketAddress getRemoteAddress();

    StreamChannelStateCode getCurrentState();

    int getStreamId();

    void init() throws StreamException;

    boolean awaitOpen(long timeoutMillis);

    void sendPing(int requestId);
    void sendPong(int requestId);

    void close();
    void close(StreamCode code);

    void disconnect();
    void disconnect(StreamCode streamCode);


    void handleStreamClosePacket(StreamClosePacket packet);

    boolean changeStateConnected();

}
