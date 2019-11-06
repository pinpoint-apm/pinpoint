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


import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;

import com.google.protobuf.Empty;

import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.grpc.trace.PStatMessage;
import com.navercorp.pinpoint.grpc.trace.StatGrpc;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.RejectedExecutionException;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author jaehong.kim
 */
public class StatGrpcDataSender extends GrpcDataSender {
    private final StatGrpc.StatStub statStub;
    private final ReconnectExecutor reconnectExecutor;

    private volatile StreamObserver<PStatMessage> statStream;
    private final Reconnector statStreamReconnector;

    public StatGrpcDataSender(String host, int port,
                              int senderExecutorQueueSize,
                              MessageConverter<GeneratedMessageV3> messageConverter,
                              ReconnectExecutor reconnectExecutor,
                              ChannelFactory channelFactory) {
        super(host, port, senderExecutorQueueSize, messageConverter, channelFactory);

        this.statStub = StatGrpc.newStub(managedChannel);
        this.reconnectExecutor = Assert.requireNonNull(reconnectExecutor, "reconnectExecutor");
        {
            final Runnable statStreamReconnectJob = new Runnable() {
                @Override
                public void run() {
                    statStream = newStatStream();
                }
            };
            this.statStreamReconnector = reconnectExecutor.newReconnector(statStreamReconnectJob);
            statStreamReconnectJob.run();
        }
    }

    private StreamObserver<PStatMessage> newStatStream() {
        final StreamId statId = StreamId.newStreamId("StatStream");
        final ResponseStreamObserver<PStatMessage, Empty> responseObserver = new ResponseStreamObserver<PStatMessage, Empty>(statId, statStreamReconnector);
        return statStub.sendAgentStat(responseObserver);
    }

    @Override
    public boolean send(final Object data) {
        final Runnable command = new Runnable() {
            @Override
            public void run() {
                try {
                    send0(data);
                } catch (Exception ex) {
                    logger.debug("send fail:{}", data, ex);
                }
            }
        };
        try {
            executor.execute(command);
        } catch (RejectedExecutionException reject) {
            logger.debug("reject:{}", command);
            return false;
        }
        return true;
    }


    private boolean send0(Object data) {
        final GeneratedMessageV3 message = messageConverter.toMessage(data);
        if (logger.isDebugEnabled()) {
            logger.debug("Send message={}", debugLog(message));
        }

        if (message instanceof PAgentStatBatch) {
            final PAgentStatBatch agentStatBatch = (PAgentStatBatch) message;
            final PStatMessage statMessage = PStatMessage.newBuilder().setAgentStatBatch(agentStatBatch).build();
            statStream.onNext(statMessage);
            return true;
        }

        if (message instanceof PAgentStat) {
            final PAgentStat agentStat = (PAgentStat) message;
            final PStatMessage statMessage = PStatMessage.newBuilder().setAgentStat(agentStat).build();
            statStream.onNext(statMessage);
            return true;
        }
        throw new IllegalStateException("unsupported message " + message);
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
        logger.info("{} close()", statStream);
        StreamUtils.close(statStream);
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