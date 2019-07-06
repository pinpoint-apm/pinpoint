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
import com.navercorp.pinpoint.grpc.trace.PCmdMessage;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdServiceHandshake;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.sender.grpc.ExponentialBackoffReconnectJob;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectJob;
import com.navercorp.pinpoint.profiler.sender.grpc.StreamUtils;

import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class GrpcCommandService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ScheduledExecutorService reconnectScheduler;

    private final ManagedChannel managedChannel;
    private final ActiveTraceRepository activeTraceRepository;

    private final ReconnectJob reconnectAction;

    private volatile boolean shutdown;

    private volatile CommandServiceMainStreamObserver commandServiceMainStreamObserver;

    public GrpcCommandService(ManagedChannel managedChannel, ScheduledExecutorService reconnectScheduler, ActiveTraceRepository activeTraceRepository) {
        this.managedChannel = Assert.requireNonNull(managedChannel, "managedChannel");
        this.reconnectScheduler = Assert.requireNonNull(reconnectScheduler, "reconnectScheduler");

        // allow null
        this.activeTraceRepository = activeTraceRepository;

        this.reconnectAction = new ExponentialBackoffReconnectJob(new Runnable() {
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
        ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub = ProfilerCommandServiceGrpc.newStub(managedChannel);
        GrpcCommandDispatcher commandDispatcher = new GrpcCommandDispatcher(profilerCommandServiceStub, activeTraceRepository);

        CommandServiceMainStreamObserver commandServiceMainStreamObserver = new CommandServiceMainStreamObserver(commandDispatcher);
        profilerCommandServiceStub.handleCommand(commandServiceMainStreamObserver);

        this.commandServiceMainStreamObserver = commandServiceMainStreamObserver;
    }

    private void reserveReconnect() {
        reconnectScheduler.schedule(reconnectAction, reconnectAction.nextBackoffNanos(), TimeUnit.NANOSECONDS);
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
                    reconnectAction.resetBackoffNanos();

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
            logger.warn("onError:{}", t.getMessage(), t);
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
