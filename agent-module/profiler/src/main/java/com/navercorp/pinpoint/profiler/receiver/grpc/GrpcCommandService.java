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

import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.client.SupportCommandCodeClientInterceptor;
import com.navercorp.pinpoint.grpc.stream.ClientCallStateStreamObserver;
import com.navercorp.pinpoint.grpc.stream.StreamUtils;
import com.navercorp.pinpoint.grpc.trace.PCmdMessage;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandServiceLocator;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;
import com.navercorp.pinpoint.profiler.sender.grpc.Reconnector;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class GrpcCommandService {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final CommandServiceStubFactory commandServiceStubFactory;
    private final ProfilerCommandServiceLocator profilerCommandServiceLocator;

    private final Reconnector reconnector;

    private volatile boolean shutdown;

    private volatile CommandServiceMainStreamObserver commandServiceMainStreamObserver;

    public GrpcCommandService(CommandServiceStubFactory commandServiceStubFactory, ReconnectExecutor reconnectScheduler, ProfilerCommandServiceLocator profilerCommandServiceLocator) {
        this.commandServiceStubFactory = Objects.requireNonNull(commandServiceStubFactory, "commandServiceStubFactory");
        Objects.requireNonNull(reconnectScheduler, "reconnectScheduler");

        this.profilerCommandServiceLocator = Objects.requireNonNull(profilerCommandServiceLocator, "profilerCommandServiceLocator");

        this.reconnector = reconnectScheduler.newReconnector(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        });

        connect();
    }

    private void connect() {
        if (shutdown) {
            logger.info("Already shutdown");
            return;
        }
        logger.info("Attempt to connect to CommandServiceStream");
        ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub = newCommandServiceStub(commandServiceStubFactory, profilerCommandServiceLocator);
        GrpcCommandDispatcher commandDispatcher = new GrpcCommandDispatcher(profilerCommandServiceStub, profilerCommandServiceLocator);

        CommandServiceMainStreamObserver commandServiceMainStreamObserver = new CommandServiceMainStreamObserver(commandDispatcher);
        profilerCommandServiceStub.handleCommandV2(commandServiceMainStreamObserver);

        this.commandServiceMainStreamObserver = commandServiceMainStreamObserver;
    }

    private ProfilerCommandServiceGrpc.ProfilerCommandServiceStub newCommandServiceStub(CommandServiceStubFactory commandServiceStubFactory, ProfilerCommandServiceLocator profilerCommandServiceLocator) {
        final ProfilerCommandServiceGrpc.ProfilerCommandServiceStub commandServiceStub = commandServiceStubFactory.newStub();

        final SupportCommandCodeClientInterceptor supportCommandCodeClientInterceptor = new SupportCommandCodeClientInterceptor(profilerCommandServiceLocator.getCommandServiceCodes());
        return commandServiceStub.withInterceptors(supportCommandCodeClientInterceptor);
    }

    private void reserveReconnect() {
        reconnector.reconnect();
    }

    public void stop() {
        logger.info("stop() started");
        if (shutdown) {
            logger.info("Already shutdown");
        }

        // It's okay to be called multiple times.
        this.shutdown = true;

        final CommandServiceMainStreamObserver observer = this.commandServiceMainStreamObserver;
        if (observer != null) {
            observer.stop();
        }
    }

    private class CommandServiceMainStreamObserver implements ClientResponseObserver<PCmdMessage, PCmdRequest> {
        private final Logger logger = LogManager.getLogger(this.getClass());

        private final GrpcCommandDispatcher commandDispatcher;
        private ClientCallStateStreamObserver<PCmdMessage> requestStream;

        public CommandServiceMainStreamObserver(GrpcCommandDispatcher commandDispatcher) {
            this.commandDispatcher = Objects.requireNonNull(commandDispatcher, "commandDispatcher");
        }

        @Override
        public void beforeStart(final ClientCallStreamObserver<PCmdMessage> stream) {

            this.requestStream = ClientCallStateStreamObserver.clientCall(stream);

            requestStream.setOnReadyHandler(new Runnable() {
                @Override
                public void run() {
                    logger.info("Connect to CommandServiceStream completed");
                    reconnector.reset();
                }
            });

        }

        @Override
        public void onNext(PCmdRequest request) {
            if (isDebug) {
                logger.debug("received request:{}", MessageFormatUtils.debugLog(request));
            }

            if (request != null) {
                commandDispatcher.handle(request, requestStream);
            }
        }

        @Override
        public void onError(Throwable t) {
            Status status = Status.fromThrowable(t);
            Metadata metadata = Status.trailersFromThrowable(t);
            logger.info("Failed to command stream, {} {}", status, metadata);

            if (requestStream.isRun()) {
                StreamUtils.onCompleted(requestStream, (th) -> logger.info("CommandService.onError", th));
            }

            reserveReconnect();
        }

        @Override
        public void onCompleted() {
            logger.info("onCompleted");

            if (requestStream.isRun()) {
                StreamUtils.onCompleted(requestStream, (th) -> logger.info("CommandService.onCompleted", th));
            }
            reserveReconnect();
        }

        private void stop() {
            logger.info("stop");
            StreamUtils.onCompleted(requestStream, (th) -> logger.info("CommandService.stop"));
            commandDispatcher.close();
        }

    }

}
