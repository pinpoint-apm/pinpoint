/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.cluster.ClusterOption;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.HealthCheckState;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateChangeEventHandler;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

import java.net.SocketAddress;
import java.util.Map;

public class StateChangeServerInterceptor implements ServerInterceptor {




    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {


        return null;
    }

    private static class StateChangePinpointServer implements PinpointServer {

        @Override
        public long getStartTimestamp() {
            return 0;
        }

        @Override
        public void messageReceived(Object message) {

        }

        @Override
        public SocketStateCode getCurrentStateCode() {
            return null;
        }

        @Override
        public HealthCheckState getHealthCheckState() {
            return null;
        }

        @Override
        public Map<Object, Object> getChannelProperties() {
            return null;
        }

        @Override
        public void send(byte[] payload) {

        }

        @Override
        public Future<ResponseMessage> request(byte[] payload) {
            return null;
        }

        @Override
        public void response(int requestId, byte[] payload) {

        }

        @Override
        public ClientStreamChannelContext openStream(byte[] payload, ClientStreamChannelMessageListener messageListener) {
            return null;
        }

        @Override
        public ClientStreamChannelContext openStream(byte[] payload, ClientStreamChannelMessageListener messageListener, StreamChannelStateChangeEventHandler<ClientStreamChannel> stateChangeListener) {
            return null;
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public void close() {
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
}
