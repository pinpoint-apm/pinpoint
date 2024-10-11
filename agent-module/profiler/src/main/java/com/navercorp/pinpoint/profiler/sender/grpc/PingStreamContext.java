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

import com.navercorp.pinpoint.grpc.stream.ClientCallStateStreamObserver;
import com.navercorp.pinpoint.grpc.stream.StreamUtils;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PPing;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PingStreamContext {
    private final Logger logger = LogManager.getLogger(this.getClass());

    // for debug
    private final StreamId streamId;

    private ClientCallStateStreamObserver<PPing> requestStream;
    private final PingClientResponseObserver responseObserver;
    private final Reconnector reconnector;

    private final ScheduledExecutorService retransmissionExecutor;
    private volatile boolean closed = false;

    public PingStreamContext(AgentGrpc.AgentStub agentStub,
                             Reconnector reconnector,
                             ScheduledExecutorService retransmissionExecutor) {
        Objects.requireNonNull(agentStub, "agentStub");

        this.streamId = StreamId.newStreamId("PingStream");

        this.reconnector = Objects.requireNonNull(reconnector, "reconnector");
        this.retransmissionExecutor = Objects.requireNonNull(retransmissionExecutor, "retransmissionExecutor");
        // WARNING
        this.responseObserver = new PingClientResponseObserver();

        agentStub.pingSession(responseObserver);
    }

    private PPing newPing() {
        return PPing.getDefaultInstance();
    }

    public boolean isClosed() {
        return closed;
    }


    private class PingClientResponseObserver implements ClientResponseObserver<PPing, PPing> {
        private volatile ScheduledFuture<?> pingScheduler;

        @Override
        public void onNext(PPing ping) {
            logger.info("Ping Response {}", streamId);
        }


        @Override
        public void onError(Throwable t) {
            final Status status = Status.fromThrowable(t);
            Metadata metadata = Status.trailersFromThrowable(t);

            logger.info("onError PingResponse {}, {} {}", streamId, status, metadata);

            dispose();

            if (requestStream.isRun()) {
                StreamUtils.onCompleted(requestStream, (th) -> logger.info("PingStreamContext.onError", th));
            }
        }


        @Override
        public void onCompleted() {
            logger.info("onCompleted {}", streamId);

            dispose();

            if (requestStream.isRun()) {
                StreamUtils.onCompleted(requestStream, (th) -> logger.info("PingStreamContext.onCompleted", th));
            }
        }

        private void dispose() {
            closed = true;
            cancelPingScheduler();
            PingStreamContext.this.reconnector.reconnect();
        }

        private void cancelPingScheduler() {
            final ScheduledFuture<?> pingScheduler = this.pingScheduler;
            if (pingScheduler != null) {
                pingScheduler.cancel(false);
            } else {
                logger.info("pingScheduler is NULL");
            }
        }

        private void registerSchedulerFuture(ScheduledFuture<?> pingScheduler) {
            synchronized (this) {
                final ScheduledFuture<?> copy = this.pingScheduler;
                if (copy != null) {
                    logger.info("registerSchedulerFuture : Cancel pingScheduler {}", streamId);
                    copy.cancel(false);
                }
                this.pingScheduler = pingScheduler;
            }
        }

        @Override
        public void beforeStart(final ClientCallStreamObserver<PPing> steram) {
            requestStream = ClientCallStateStreamObserver.clientCall(steram);

            requestStream.setOnReadyHandler(new Runnable() {
                @Override
                public void run() {
                    logger.info("onReadyHandler {}", streamId);
                    PingStreamContext.this.reconnector.reset();

                    final Runnable pingRunnable = new Runnable() {
                        @Override
                        public void run() {
                            PPing pPing = newPing();
                            if (requestStream.isReady()) {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("Send Ping {}", streamId);
                                }
                                requestStream.onNext(pPing);
                            } else {
                                logger.debug("Send Ping failed. isReady=false {}", streamId);
                            }
                        }
                    };

                    registerSchedulerFuture(schedule(pingRunnable));
                }
            });
        }
    }

    private ScheduledFuture<?> schedule(Runnable command) {
        try {
            return retransmissionExecutor.scheduleAtFixedRate(command, 0, 1, TimeUnit.MINUTES);
        } catch (RejectedExecutionException e) {
            logger.info("Ping scheduling failed");
            return null;
        }
    }

    public void close() {
        logger.info("close() {}", streamId);
        StreamUtils.onCompleted(this.requestStream, (th) -> this.logger.info("PingStreamContext.close", th));
    }

    @Override
    public String toString() {
        return "PingStreamContext{" +
                streamId +
                '}';
    }
}
