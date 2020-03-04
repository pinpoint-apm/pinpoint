/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.grpc;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.StatusError;
import com.navercorp.pinpoint.grpc.StatusErrors;
import com.navercorp.pinpoint.grpc.trace.PCmdMessage;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdServiceHandshake;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;
import com.navercorp.pinpoint.profiler.sender.grpc.Reconnector;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamUtils;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class GrpcCommandService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final CommandServiceStubFactory commandServiceStubFactory;
    private final ActiveTraceRepository activeTraceRepository;

    private final Reconnector reconnector;

    private volatile boolean shutdown;

    private volatile CommandServiceMainStreamObserver commandServiceMainStreamObserver;

    public GrpcCommandService(CommandServiceStubFactory commandServiceStubFactory, ReconnectExecutor reconnectScheduler, ActiveTraceRepository activeTraceRepository) {
        this.commandServiceStubFactory = Assert.requireNonNull(commandServiceStubFactory, "commandServiceStubFactory");
        Assert.requireNonNull(reconnectScheduler, "reconnectScheduler");

        // allow null
        this.activeTraceRepository = activeTraceRepository;

        this.reconnector = reconnectScheduler.newReconnector(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        });

        connect();
    }

    private void connect() {
        logger.info("Attempt to connect to CommandServiceStream.");
        if (shutdown) {
            return;
        }
        ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub = commandServiceStubFactory.newStub();
        GrpcCommandDispatcher commandDispatcher = new GrpcCommandDispatcher(profilerCommandServiceStub, activeTraceRepository);

        CommandServiceMainStreamObserver commandServiceMainStreamObserver = new CommandServiceMainStreamObserver(commandDispatcher);
        profilerCommandServiceStub.handleCommand(commandServiceMainStreamObserver);

        this.commandServiceMainStreamObserver = commandServiceMainStreamObserver;
    }

    private void reserveReconnect() {
        reconnector.reconnect();
    }

    public void stop() {
        logger.info("stop() started");
        if (!shutdown) {
            // It's okay to be called multiple times.
            this.shutdown = true;

            final CommandServiceMainStreamObserver commandServiceMainStreamObserver = this.commandServiceMainStreamObserver;
            if (commandServiceMainStreamObserver != null) {
                commandServiceMainStreamObserver.stop();
            }
        }
    }

    private class CommandServiceMainStreamObserver implements ClientResponseObserver<PCmdMessage, PCmdRequest> {

        private final GrpcCommandDispatcher commandDispatcher;
        private ClientCallStreamObserver<PCmdMessage> requestStream;

        public CommandServiceMainStreamObserver(GrpcCommandDispatcher commandDispatcher) {
            this.commandDispatcher = Assert.requireNonNull(commandDispatcher, "commandDispatcher");
        }

        @Override
        public void beforeStart(final ClientCallStreamObserver<PCmdMessage> requestStream) {
            this.requestStream = requestStream;

            requestStream.setOnReadyHandler(new Runnable() {
                @Override
                public void run() {
                    logger.info("Connect to CommandServiceStream completed.");
                    reconnector.reset();

                    PCmdServiceHandshake.Builder handshakeMessageBuilder = PCmdServiceHandshake.newBuilder();
                    for (Short commandServiceCode : commandDispatcher.getSupportCommandServiceIdList()) {
                        handshakeMessageBuilder.addSupportCommandServiceKey(commandServiceCode);
                    }

                    PCmdMessage.Builder initialMessage = PCmdMessage.newBuilder();
                    initialMessage.setHandshakeMessage(handshakeMessageBuilder.build());

                    requestStream.onNext(initialMessage.build());
                }
            });

        }

        @Override
        public void onNext(PCmdRequest request) {
            if (isDebug) {
                logger.debug("received request:{}", request);
            }

            if (request != null) {
                commandDispatcher.handle(request, requestStream);
            }
        }

        @Override
        public void onError(Throwable t) {
            final StatusError statusError = StatusErrors.throwable(t);
            if (statusError.isSimpleError()) {
                logger.info("Failed to command stream, cause={}", statusError.getMessage());
            } else {
                logger.warn("Failed to command stream, cause={}", statusError.getMessage(), statusError.getThrowable());
            }

            if (requestStream != null) {
                requestStream.onError(t);
            }

            commandDispatcher.close();
            reserveReconnect();
        }

        @Override
        public void onCompleted() {
            logger.info("onCompleted");
            StreamUtils.close(requestStream);
            commandDispatcher.close();
            // TODO : needs to check whether needs new action
            reserveReconnect();
        }

        private void stop() {
            logger.info("stop");
            StreamUtils.close(requestStream);
            commandDispatcher.close();
        }

    }

}
