/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender.grpc;


import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PCustomMetricMessage;
import com.navercorp.pinpoint.grpc.trace.PStatMessage;
import com.navercorp.pinpoint.grpc.trace.StatGrpc;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.ClientStreamingProvider;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.DefaultStreamTask;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.StreamExecutorFactory;
import com.navercorp.pinpoint.profiler.util.NamedRunnable;
import io.grpc.stub.ClientCallStreamObserver;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author jaehong.kim
 */
public class StatGrpcDataSender extends GrpcDataSender {

    private final ReconnectExecutor reconnectExecutor;

    private final Reconnector reconnector;
    private final StreamState failState;
    private final StreamExecutorFactory<PStatMessage> streamExecutorFactory;
    private final String id = "StatStream";

    private volatile StreamTask<PStatMessage> currentStreamTask;

    private final ClientStreamingService<PStatMessage, Empty> clientStreamService;

    public MessageDispatcher<PStatMessage> dispatcher = new MessageDispatcher<PStatMessage>() {
        @Override
        public void onDispatch(ClientCallStreamObserver<PStatMessage> stream, Object data) {
            final GeneratedMessageV3 message = messageConverter.toMessage(data);
            if (isDebug) {
                logger.debug("Send message={}", debugLog(message));
            }

            if (message instanceof PAgentStatBatch) {
                final PAgentStatBatch agentStatBatch = (PAgentStatBatch) message;
                final PStatMessage statMessage = PStatMessage.newBuilder().setAgentStatBatch(agentStatBatch).build();
                stream.onNext(statMessage);
                return;
            }

            if (message instanceof PAgentStat) {
                final PAgentStat agentStat = (PAgentStat) message;
                final PStatMessage statMessage = PStatMessage.newBuilder().setAgentStat(agentStat).build();
                stream.onNext(statMessage);
                return;
            }
            if (message instanceof PCustomMetricMessage) {
                final PCustomMetricMessage customMetricMessage = (PCustomMetricMessage) message;
                logger.info("Message will not delivered. message:{}", message);

                return;
            }
            if (message instanceof PAgentUriStat) {
                final PAgentUriStat agentUriStat = (PAgentUriStat) message;
                final PStatMessage statMessage = PStatMessage.newBuilder().setAgentUriStat(agentUriStat).build();

                // TODO remove comment
                stream.onNext(statMessage);
                return;
            }
            throw new IllegalStateException("unsupported message " + message);
        }
    };

    public StatGrpcDataSender(String host, int port,
                              int executorQueueSize,
                              MessageConverter<GeneratedMessageV3> messageConverter,
                              ReconnectExecutor reconnectExecutor,
                              ChannelFactory channelFactory) {
        super(host, port, executorQueueSize, messageConverter, channelFactory);

        this.reconnectExecutor = Assert.requireNonNull(reconnectExecutor, "reconnectExecutor");
        final Runnable reconnectJob = new NamedRunnable(this.id) {
            @Override
            public void run() {
                startStream();
            }
        };
        this.reconnector = reconnectExecutor.newReconnector(reconnectJob);
        this.failState = new SimpleStreamState(100, 5000);
        this.streamExecutorFactory = new StreamExecutorFactory<PStatMessage>(executor);

        ClientStreamingProvider<PStatMessage, Empty> clientStreamProvider = new ClientStreamingProvider<PStatMessage, Empty>() {
            @Override
            public ClientCallStreamObserver<PStatMessage> newStream(ResponseStreamObserver<PStatMessage, Empty> response) {
                logger.info("newStream {}", id);
                StatGrpc.StatStub statStub = StatGrpc.newStub(managedChannel);
                return (ClientCallStreamObserver<PStatMessage>) statStub.sendAgentStat(response);
            }
        };
        this.clientStreamService = new ClientStreamingService<PStatMessage, Empty >(clientStreamProvider, reconnector);

        reconnectJob.run();

    }

    private void startStream() {
//        streamTaskManager.closeAllStream();
        try {
            StreamTask<PStatMessage> streamTask =  new DefaultStreamTask<PStatMessage, Empty>(id, clientStreamService,
                    this.streamExecutorFactory, this.queue, this.dispatcher, failState);
            streamTask.start();
            this.currentStreamTask = streamTask;
        } catch (Throwable th) {
            logger.error("Unexpected error", th);
        }
    }

    @Override
    public void stop() {
        if (shutdown) {
            return;
        }
        this.shutdown = true;

        logger.info("Stop {}, channel={}", name, managedChannel);
        final ReconnectExecutor reconnectExecutor = this.reconnectExecutor;
        if (reconnectExecutor != null) {
            reconnectExecutor.close();
        }

        final StreamTask<PStatMessage> currentStreamTask = this.currentStreamTask;
        if (currentStreamTask != null) {
            currentStreamTask.stop();
        }
        logger.info("{} close()", id);
        release();
    }

    @Override
    public String toString() {
        return "StatGrpcDataSender{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                "} " + super.toString();
    }

}