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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.grpc.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.grpc.trace.StatGrpc;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.NameResolverProvider;
import io.grpc.stub.StreamObserver;

/**
 * @author jaehong.kim
 */
public class StatGrpcDataSender extends GrpcDataSender {
    private final StatGrpc.StatStub statStub;

    private volatile StreamObserver<PAgentStat> statStream;
    private final ReconnectJob statStreamReconnectAction;

    private volatile StreamObserver<PAgentStatBatch> statBatchStream;
    private final ReconnectJob statBatchStreamReconnectAction;

    public StatGrpcDataSender(String name, String host, int port, MessageConverter<GeneratedMessageV3> messageConverter, HeaderFactory headerFactory, NameResolverProvider nameResolverProvider) {
        super(name, host, port, messageConverter, headerFactory, nameResolverProvider);

        this.statStub = StatGrpc.newStub(managedChannel);

        statStreamReconnectAction = new ExponentialBackoffReconnectJob() {
            @Override
            public void run() {
                statStream = newStatStream();
            }
        };
        this.statStream = newStatStream();

        statBatchStreamReconnectAction = new ExponentialBackoffReconnectJob() {
            @Override
            public void run() {
                statBatchStream = newStatBatchStream();
            }
        };
        this.statBatchStream = newStatBatchStream();
    }

    private StreamObserver<PAgentStat> newStatStream() {
        final ResponseStreamObserver<PAgentStat> responseObserver = new ResponseStreamObserver(statStreamReconnectAction);
        return statStub.sendAgentStat(responseObserver);
    }

    private StreamObserver<PAgentStatBatch> newStatBatchStream() {
        final ResponseStreamObserver<PAgentStatBatch> responseObserver = new ResponseStreamObserver(statBatchStreamReconnectAction);
        return statStub.sendAgentStatBatch(responseObserver);
    }

    public boolean send0(Object data) {
        final GeneratedMessageV3 message = messageConverter.toMessage(data);
        if (logger.isDebugEnabled()) {
            logger.debug("Stat send0 data={}", data);
        }

        if (message instanceof PAgentStatBatch) {
            final PAgentStatBatch agentStatBatch = (PAgentStatBatch) message;
            statBatchStream.onNext(agentStatBatch);
            return true;
        }
        if (message instanceof PAgentStat) {
            final PAgentStat agentStat = (PAgentStat) message;
            statStream.onNext(agentStat);
            return true;
        }
        throw new IllegalStateException("unsupported message " + message);
    }
}