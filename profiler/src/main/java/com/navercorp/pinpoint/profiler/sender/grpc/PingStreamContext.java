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
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.StatusError;
import com.navercorp.pinpoint.grpc.StatusErrors;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PPing;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PingStreamContext {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // for debug
    private final StreamId streamId;

    private final StreamObserver<PPing> requestObserver;
    private final PingClientResponseObserver responseObserver;
    private final Reconnector reconnector;

    private final ScheduledExecutorService retransmissionExecutor;

    public PingStreamContext(AgentGrpc.AgentStub agentStub,
                             Reconnector reconnector,
                             ScheduledExecutorService retransmissionExecutor) {
        Assert.requireNonNull(agentStub, "agentStub");

        this.streamId = StreamId.newStreamId("PingStream");

        this.responseObserver = new PingClientResponseObserver();
        this.requestObserver = agentStub.pingSession(responseObserver);

        this.reconnector = Assert.requireNonNull(reconnector, "reconnector");
        this.retransmissionExecutor = Assert.requireNonNull(retransmissionExecutor, "retransmissionExecutor");
    }

    private PPing newPing() {
        PPing.Builder builder = PPing.newBuilder();
        return builder.build();
    }


    private class PingClientResponseObserver implements ClientResponseObserver<PPing, PPing> {
        private volatile ScheduledFuture<?> pingScheduler;

        @Override
        public void onNext(PPing ping) {
            logger.info("{} success:{}", streamId, MessageFormatUtils.debugLog(ping));

        }


        @Override
        public void onError(Throwable t) {
            final StatusError statusError = StatusErrors.throwable(t);
            if (statusError.isSimpleError()) {
                logger.info("Failed to ping stream, streamId={}, cause={}", streamId, statusError.getMessage());
            } else {
                logger.warn("Failed to ping stream, streamId={}, cause={}", streamId, statusError.getMessage(), statusError.getThrowable());
            }
            cancelPingScheduler();
            reconnector.reconnect();
        }


        @Override
        public void onCompleted() {
            logger.info("{} completed", streamId);
            cancelPingScheduler();
            reconnector.reconnect();
        }

        private void cancelPingScheduler() {
            final ScheduledFuture<?> pingScheduler = this.pingScheduler;
            if (pingScheduler != null) {
                pingScheduler.cancel(false);
            } else {
                logger.info("pingScheduler is NULL");
            }
        }

        @Override
        public void beforeStart(final ClientCallStreamObserver<PPing> requestStream) {
            requestStream.setOnReadyHandler(new Runnable() {
                @Override
                public void run() {
                    logger.info("{} onReady", streamId);
                    reconnector.reset();

                    final Runnable pingRunnable = new Runnable() {
                        @Override
                        public void run() {
                            PPing pPing = newPing();
                            requestStream.onNext(pPing);
                        }
                    };

                    PingClientResponseObserver.this.pingScheduler = schedule(pingRunnable);
                }
            });
        }
    };

    private ScheduledFuture<?> schedule(Runnable command) {
        try {
            return retransmissionExecutor.scheduleAtFixedRate(command, 0, 1,TimeUnit.MINUTES);
        } catch (RejectedExecutionException e) {
            logger.info("Ping scheduling failed");
            return null;
        }
    }

    public void close() {
        logger.info("{} close()", streamId);
        StreamUtils.close(this.requestObserver);
    }

    @Override
    public String toString() {
        return "PingStreamContext{" +
                streamId +
                '}';
    }
}
