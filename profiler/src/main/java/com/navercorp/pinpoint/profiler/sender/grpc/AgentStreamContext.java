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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PResult;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentStreamContext implements StreamObserver<PAgentInfo> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StreamObserver<PAgentInfo> requestObserver;
    private final AgentClientResponseObserver responseObserver;
    private final Reconnector reconnector;
    private final AgentInfoSupplier agentInfoSupplier;

    private final RetransmissionExecutor retransmissionExecutor;

    public AgentStreamContext(AgentGrpc.AgentStub agentStub,
                              AgentInfoSupplier agentInfoSupplier,
                              Reconnector reconnector) {
        Assert.requireNonNull(agentStub, "agentStub must not be null");

        this.responseObserver = new AgentClientResponseObserver();
        this.requestObserver = agentStub.sendAgentInfo(responseObserver);

        this.agentInfoSupplier = Assert.requireNonNull(agentInfoSupplier, "agentInfoSupplier must not be null");
        this.reconnector = Assert.requireNonNull(reconnector, "reconnector must not be null");

        this.retransmissionExecutor = new RetransmissionExecutor();
    }

    public ListenableFuture<PResult> getResponseFuture() {
        return responseObserver.getResponseFuture();
    }

    @Override
    public void onNext(PAgentInfo value) {
        this.requestObserver.onNext(value);
    }

    @Override
    public void onError(Throwable t) {
        this.requestObserver.onError(t);
    }

    @Override
    public void onCompleted() {
        this.requestObserver.onCompleted();
    }

    private class AgentClientResponseObserver implements ClientResponseObserver<PAgentInfo, PResult> {

        private final SettableFuture<PResult> responseSuccess = SettableFuture.create();

        @Override
        public void onNext(PResult value) {
            if (value.getSuccess()) {
                logger.info("agent response success {}", MessageFormatUtils.debugLog(value));
                responseSuccess.set(value);
                retransmissionExecutor.close();
            } else {
                logger.info("agent response fail {}", MessageFormatUtils.debugLog(value));
            }
        }

        public SettableFuture<PResult> getResponseFuture() {
            return responseSuccess;
        }

        @Override
        public void onError(Throwable t) {
            logger.info("agent-stream error Caused by:{}", t.getMessage(), t);
            reconnector.reconnect();
        }

        public boolean isResponseSuccess() {
            try {
                // check for avoid thead block
                if (!responseSuccess.isDone()) {
                    return false;
                }

                final PResult pResult = responseSuccess.get();
                return pResult.getSuccess();
            } catch (InterruptedException e) {
                logger.info("unexpected error", e);
                return false;
            } catch (ExecutionException e) {
                logger.info("unexpected error", e);
                return false;
            }
        }

        @Override
        public void onCompleted() {
            logger.info("agent-stream close");
            reconnector.reconnect();
        }

        @Override
        public void beforeStart(final ClientCallStreamObserver<PAgentInfo> requestStream) {
            requestStream.setOnReadyHandler(new Runnable() {
                @Override
                public void run() {
                    logger.info("inReadyHandler.run()");
                    reconnector.reset();
                    final Runnable sendAgentInfo = new Runnable() {
                        @Override
                        public void run() {
                            final ClientCallStreamObserver<PAgentInfo> copy = requestStream;
                            if (!copy.isReady()) {
                                logger.info("isReady() == false stream:{}", copy);
                                return;
                            }
                            final PAgentInfo pAgentInfo = agentInfoSupplier.get();
                            if (pAgentInfo != null) {
                                logger.info("sendAgentInfo {} {}", pAgentInfo, copy);
                                copy.onNext(pAgentInfo);
                            }
                        }
                    };
                    sendAgentInfo.run();
                    retransmissionExecutor.execute(sendAgentInfo);
                }
            });
        }
    };

    class RetransmissionExecutor implements Executor {
        private volatile boolean shutdown;
        @Override
        public void execute(final Runnable command) {
            Assert.requireNonNull(command, "command must not be null");
            if (shutdown) {
                return;
            }

            Runnable retryJob = new Runnable() {
                @Override
                public void run() {
                    if (responseObserver.isResponseSuccess()) {
                        return;
                    }
                    command.run();
                    schedule(command);
                }
            };
            schedule(retryJob);
        }

        private void schedule(Runnable command) {
            // TODO
            GrpcDataSender.reconnectScheduler.schedule(command, 3, TimeUnit.SECONDS);
        }

        public void close() {
            logger.info("close {}", this.getClass().getName());
            shutdown = true;
        }
    };
}
