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

import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.MetadataGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import io.grpc.Attributes;
import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ConnectivityStateInfo;
import io.grpc.EquivalentAddressGroup;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.grpc.ConnectivityState.CONNECTING;
import static io.grpc.ConnectivityState.SHUTDOWN;
import static io.grpc.ConnectivityState.TRANSIENT_FAILURE;

/**
 * @author jaehong.kim
 */
public class AgentClientMock {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManagedChannel channel;
    private final AgentGrpc.AgentStub agentStub;
    private final MetadataGrpc.MetadataBlockingStub metadataStub;


    public AgentClientMock(final String host, final int port, final boolean agentHeader) {
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);

        if (agentHeader) {
            HeaderFactory headerFactory = new AgentHeaderFactory("mockAgentId", "mockApplicationName", System.currentTimeMillis());
            final Metadata extraHeaders = headerFactory.newHeader();
            final ClientInterceptor headersInterceptor = MetadataUtils.newAttachHeadersInterceptor(extraHeaders);
            builder.intercept(headersInterceptor);
        }
        builder.usePlaintext();
        channel = builder.build();
        this.agentStub = AgentGrpc.newStub(channel);
        this.metadataStub = MetadataGrpc.newBlockingStub(channel);
    }

    public void stop() throws InterruptedException {
        stop(5);
    }

    public void stop(long await) throws InterruptedException {
        channel.shutdown().awaitTermination(await, TimeUnit.SECONDS);
    }

    public void info() {
        info(1);
    }

    public void info(final int count) {
        for (int i = 0; i < count; i++) {
            PAgentInfo request = PAgentInfo.newBuilder().build();
            QueueingStreamObserver<PResult> responseObserver = getResponseObserver();
            agentStub.requestAgentInfo(request, responseObserver);
            PResult value = responseObserver.getValue();
            logger.info("Result {}", value);
        }
    }

    public void apiMetaData() throws InterruptedException {
        apiMetaData(1);
    }

    public void apiMetaData(final int count) {
        for (int i = 0; i < count; i++) {
            PApiMetaData request = PApiMetaData.newBuilder().build();
            PResult result = metadataStub.requestApiMetaData(request);
        }
    }

    public void sqlMetaData() throws InterruptedException {
        sqlMetaData(1);
    }

    public void sqlMetaData(final int count) {
        for (int i = 0; i < count; i++) {
            PSqlMetaData request = PSqlMetaData.newBuilder().build();
            PResult result = metadataStub.requestSqlMetaData(request);
        }
    }

    public void stringMetaData() throws InterruptedException {
        stringMetaData(1);
    }

    public void stringMetaData(final int count) {
        for (int i = 0; i < count; i++) {
            PStringMetaData request = PStringMetaData.newBuilder().build();
            PResult result = metadataStub.requestStringMetaData(request);
        }
    }


    private <T> QueueingStreamObserver<T> getResponseObserver() {
        return new QueueingStreamObserver<>();
    }

    class QueueingStreamObserver<V> implements StreamObserver<V> {
        private final BlockingQueue<V> queue = new ArrayBlockingQueue<V>(1024);

        @Override
        public void onNext(V value) {
            logger.info("Response {}", value);
            queue.add(value);
        }

        public V getValue() {
            try {
                return queue.poll(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        @Override
        public void onError(Throwable throwable) {
            logger.info("Error ", throwable);
        }

        @Override
        public void onCompleted() {
            logger.info("Completed");
        }
    }

    public class CustomLoadBalancerFactory extends LoadBalancer.Factory {
        @Override
        public LoadBalancer newLoadBalancer(LoadBalancer.Helper helper) {
            return new CustomLoadBalancer(helper);
        }
    }

    static class CustomLoadBalancer extends LoadBalancer {
        private final Helper helper;
        private Subchannel subchannel;

        public CustomLoadBalancer(Helper helper) {
            this.helper = helper;
        }

        @Override
        public void handleResolvedAddressGroups(List<EquivalentAddressGroup> servers, Attributes attributes) {
            if (subchannel == null) {
                subchannel = helper.createSubchannel(servers, Attributes.EMPTY);

                // The channel state does not get updated when doing name resolving today, so for the moment
                // let LB report CONNECTION and call subchannel.requestConnection() immediately.
                helper.updateBalancingState(CONNECTING, new Picker(PickResult.withSubchannel(subchannel)));
                subchannel.requestConnection();
            } else {
                helper.updateSubchannelAddresses(subchannel, servers);
            }
        }

        @Override
        public void handleNameResolutionError(Status error) {
            if (subchannel != null) {
                subchannel.shutdown();
                subchannel = null;
            }
            // NB(lukaszx0) Whether we should propagate the error unconditionally is arguable. It's fine
            // for time being.
            helper.updateBalancingState(TRANSIENT_FAILURE, new Picker(PickResult.withError(error)));
        }

        @Override
        public void handleSubchannelState(Subchannel subchannel, ConnectivityStateInfo stateInfo) {
            ConnectivityState currentState = stateInfo.getState();
            if (subchannel != this.subchannel || currentState == SHUTDOWN) {
                return;
            }

            PickResult pickResult;
            switch (currentState) {
                case CONNECTING:
                    pickResult = PickResult.withNoResult();
                    break;
                case READY:
                case IDLE:
                    pickResult = PickResult.withSubchannel(subchannel);
                    break;
                case TRANSIENT_FAILURE:
                    pickResult = PickResult.withError(stateInfo.getStatus());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported state:" + currentState);
            }

            helper.updateBalancingState(currentState, new Picker(pickResult));
        }

        @Override
        public void shutdown() {
            if (subchannel != null) {
                subchannel.shutdown();
            }
        }
    }

    static final class Picker extends LoadBalancer.SubchannelPicker {
        private final LoadBalancer.PickResult result;

        Picker(LoadBalancer.PickResult result) {
            this.result = checkNotNull(result, "result");
        }

        @Override
        public LoadBalancer.PickResult pickSubchannel(LoadBalancer.PickSubchannelArgs args) {
            return result;
        }

        @Override
        public void requestConnection() {
            LoadBalancer.Subchannel subchannel = result.getSubchannel();
            if (subchannel != null) {
                subchannel.requestConnection();
            }
        }
    }
}