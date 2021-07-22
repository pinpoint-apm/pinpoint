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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamException;

import java.net.SocketAddress;

/**
 * @author emeroad
 * @author netspider
 */
public interface PinpointClientHandler {

    void initReconnect();

    ConnectFuture getConnectFuture();
    
    void setPinpointClient(PinpointClient pinpointClient);

    void sendSync(byte[] bytes);

    Future sendAsync(byte[] bytes);

    void close();

    void send(byte[] bytes);

    Future<ResponseMessage> request(byte[] bytes);

    void response(int requestId, byte[] payload);

    ClientStreamChannel openStream(byte[] payload, ClientStreamChannelEventHandler streamChannelEventHandler) throws StreamException;
    ClientStreamChannel openStreamAndAwait(byte[] payload, ClientStreamChannelEventHandler streamChannelEventHandler, long timeout) throws StreamException;

    void sendPing();

    boolean isConnected();

    SocketStateCode getCurrentStateCode();

    SocketAddress getRemoteAddress();

    ClusterOption getLocalClusterOption();
    ClusterOption getRemoteClusterOption();

}
