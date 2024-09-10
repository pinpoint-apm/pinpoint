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
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.SpanType;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

/**
 * @author jaehong.kim
 */
public class SimpleSpanGrpcDataSender extends GrpcDataSender<SpanType> {

    private final static String id = "SimpleSpanStream";
    private final AtomicLong streamId = new AtomicLong(0);
    private final Thread dispathThread;
    private final StreamState state;

    public SimpleSpanGrpcDataSender(String host, int port,
                              int executorQueueSize,
                              MessageConverter<SpanType, GeneratedMessageV3> messageConverter,
                              ChannelFactory channelFactory,
                              StreamState state,
                              long maxRpcAgeMillis) {
        super(host, port, executorQueueSize, messageConverter, channelFactory);
        this.state = Objects.requireNonNull(state, "state");

        this.dispathThread = new Thread(this::dispatch, "Pinpoint grpc-span-dispatch");
        this.dispathThread.start();
    }

    private void dispatch() {
        FinishStateResponseObserver<Empty> response = new FinishStateResponseObserver<>(logger);
        ClientCallStreamObserver<PSpanMessage> callStream = newStream(response);
        while (!shutdown) {
            try {
                final SpanType message = queue.poll(3000, TimeUnit.MILLISECONDS);
                if (message != null) {
                    logger.debug("--------------dispatch:{}", message);
                    boolean ready = callStream.isReady();
                    boolean runState = response.getState().isRun();
                    if (ready && runState) {
                        try {
                            onDispatch(callStream, message);
                        } catch (Throwable th) {
                            logger.warn("Unexpected onDispatch error error", th);
                        }
                        state.success();
                    } else {
                        logger.info("dispatch failed isReady:{}, runState:{}", ready, runState);
                        state.fail();
                    }
                }
                if (state.isFailure()) {
                    logger.info("renewStream: {}", this);
                    long failCount = state.getFailCount();
                    if (failCount == 0) {
                        callStream.onCompleted();
                    } else {
                        String errorMessage = "failState detected failCount" + failCount;
                        callStream.cancel(errorMessage, new Exception(errorMessage));
                    }
                    response = new FinishStateResponseObserver<>(logger);
                    callStream = newStream(response);
                }
                continue;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // terminate signal
                logger.debug("Dispatch thread interrupted {}", Thread.currentThread().getName());
                break;
            } catch (Throwable th) {
                logger.error("Unexpected outer onDispatch error", th);
            }
            logger.info("dispatch finished");
//            callStream.onCompleted();
        }
    }

    public ClientCallStreamObserver<PSpanMessage> newStream(StreamObserver<Empty> response) {
        final ManagedChannel managedChannel = this.managedChannel;
        String authority = managedChannel.authority();
        final ConnectivityState state = managedChannel.getState(false);
        this.logger.info("newStream {}/{} state:{} isShutdown:{} isTerminated:{}", id, authority, state, managedChannel.isShutdown(), managedChannel.isTerminated());

        SpanGrpc.SpanStub spanStub = SpanGrpc.newStub(managedChannel);
        return (ClientCallStreamObserver<PSpanMessage>) spanStub.sendSpan(response);
    }

    public void onDispatch(ClientCallStreamObserver<PSpanMessage> stream, SpanType data) {
        final GeneratedMessageV3 message = this.messageConverter.toMessage(data);
        if (isDebug) {
            logger.debug("Send message={}", debugLog(message));
        }
        final PSpanMessage.Builder builder = PSpanMessage.newBuilder();
        if (message instanceof PSpanChunk) {
            final PSpanChunk spanChunk = (PSpanChunk) message;
            final PSpanMessage spanMessage = builder.setSpanChunk(spanChunk).build();
            stream.onNext(spanMessage);
            return;
        }
        if (message instanceof PSpan) {
            final PSpan pSpan = (PSpan) message;
            final PSpanMessage spanMessage = builder.setSpan(pSpan).build();
            stream.onNext(spanMessage);
            return;
        }
        throw new IllegalStateException("unsupported message " + data);
    }

    @Override
    public boolean send(SpanType data) {
        return super.send(data);
    }


    @Override
    public void stop() {
        if (shutdown) {
            return;
        }
        this.shutdown = true;

        try {
            this.dispathThread.interrupt();
            this.dispathThread.join(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Stop {}, channel={}", name, managedChannel);

//        StreamUtils.close(this.stream);
        release();
    }

    @Override
    public String toString() {
        return "SpanGrpcDataSender{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                "} " + super.toString();
    }


}