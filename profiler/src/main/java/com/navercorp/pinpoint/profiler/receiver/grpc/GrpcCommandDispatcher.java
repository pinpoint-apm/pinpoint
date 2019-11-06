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

import com.google.protobuf.StringValue;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.trace.PCmdMessage;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandLocatorBuilder;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandService;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandServiceLocator;
import com.navercorp.pinpoint.profiler.receiver.ProfilerSimpleCommandService;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Collection;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class GrpcCommandDispatcher {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerCommandServiceLocator commandServiceLocator;

    public GrpcCommandDispatcher(ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub) {
        this(profilerCommandServiceStub, null);
    }

    public GrpcCommandDispatcher(ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub, ActiveTraceRepository activeTraceRepository) {
        Assert.requireNonNull(profilerCommandServiceStub, "profilerCommandServiceStub");

        ProfilerCommandLocatorBuilder profilerCommandLocatorBuilder = new ProfilerCommandLocatorBuilder();
        profilerCommandLocatorBuilder.addService(new GrpcEchoService(profilerCommandServiceStub));
        if (activeTraceRepository != null) {
            profilerCommandLocatorBuilder.addService(new GrpcActiveThreadCountService(profilerCommandServiceStub, activeTraceRepository));
            profilerCommandLocatorBuilder.addService(new GrpcActiveThreadDumpService(profilerCommandServiceStub, activeTraceRepository));
            profilerCommandLocatorBuilder.addService(new GrpcActiveThreadLightDumpService(profilerCommandServiceStub, activeTraceRepository));
        }

        this.commandServiceLocator = profilerCommandLocatorBuilder.build();
    }

    public void handle(PCmdRequest commandRequest, StreamObserver<PCmdMessage> streamObserver) {
        int value = commandRequest.getCommandCase().getNumber();

        ProfilerSimpleCommandService grpcService = commandServiceLocator.getSimpleService((short) value);
        if (grpcService != null) {
            try {
                grpcService.simpleCommandService(commandRequest);
            } catch (Exception e) {
                logger.warn("Failed to handle commandService. message:{}", e.getMessage(), e);
                PCmdResponse failMessage = createFailMessage(commandRequest, e.getMessage());
                if (streamObserver != null) {
                    streamObserver.onNext(PCmdMessage.newBuilder().setFailMessage(failMessage).build());
                }
            }
        } else {
            PCmdResponse failMessage = createFailMessage(commandRequest, TRouteResult.NOT_SUPPORTED_REQUEST.name());
            if (streamObserver != null) {
                streamObserver.onNext(PCmdMessage.newBuilder().setFailMessage(failMessage).build());
            }
        }

    }

    private PCmdResponse createFailMessage(PCmdRequest commandRequest, String message) {
        PCmdResponse.Builder failMessage = PCmdResponse.newBuilder();
        failMessage.setResponseId(commandRequest.getRequestId());
        failMessage.setMessage(StringValue.of(message));
        return failMessage.build();
    }

    public Collection<Short> getSupportCommandServiceIdList() {
        return commandServiceLocator.getCommandServiceCodes();
    }

    public void close() {
        logger.info("close() started");

        Set<Short> commandServiceCodes = commandServiceLocator.getCommandServiceCodes();
        for (Short commandServiceCode : commandServiceCodes) {
            ProfilerCommandService service = commandServiceLocator.getService(commandServiceCode);
            if (service instanceof Closeable) {
                try {
                    ((Closeable) service).close();
                } catch (Exception e) {
                    logger.warn("failed to close for CommandService:{}. message:{}", service, e.getMessage());
                }
            }
        }

        logger.info("close() completed");
    }

}
