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

import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.ConnectFuture.Result;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;

import java.net.SocketAddress;

/**
 * @author emeroad
 * @author netspider
 */
public class ReconnectStateClientHandler implements PinpointClientHandler {

    private static final ConnectFuture failedConnectFuture = new ConnectFuture();
    static {
        failedConnectFuture.setResult(Result.FAIL);
    }

    private volatile SocketStateCode state = SocketStateCode.BEING_CONNECT;
    

    @Override
    public void initReconnect() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ConnectFuture getConnectFuture() {
        return failedConnectFuture;
    }
    
    @Override
    public void setPinpointClient(PinpointClient pinpointClient) {
    }

    @Override
    public void sendSync(byte[] bytes) {
        throw newReconnectException();
    }

    @Override
    public Future sendAsync(byte[] bytes) {
        return reconnectFailureFuture();
    }

    private DefaultFuture<ResponseMessage> reconnectFailureFuture() {
        DefaultFuture<ResponseMessage> reconnect = new DefaultFuture<ResponseMessage>();
        reconnect.setFailure(newReconnectException());
        return reconnect;
    }

    @Override
    public void close() {
        this.state = SocketStateCode.CLOSED_BY_CLIENT;
    }

    @Override
    public void send(byte[] bytes) {
    }

    private PinpointSocketException newReconnectException() {
        return new PinpointSocketException("reconnecting...");
    }

    @Override
    public Future<ResponseMessage> request(byte[] bytes) {
        return reconnectFailureFuture();
    }

    @Override
    public void response(int requestId, byte[] payload) {

    }

    @Override
    public ClientStreamChannel openStream(byte[] payload, ClientStreamChannelEventHandler streamChannelEventHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendPing() {
    }

    @Override
    public SocketStateCode getCurrentStateCode() {
        return state;
    }
    
    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public ClusterOption getLocalClusterOption() {
        return null;
    }

    @Override
    public ClusterOption getRemoteClusterOption() {
        return null;
    }

}
