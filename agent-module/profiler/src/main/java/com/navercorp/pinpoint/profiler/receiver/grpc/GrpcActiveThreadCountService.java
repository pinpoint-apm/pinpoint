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

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdStreamResponse;
import com.navercorp.pinpoint.grpc.trace.PCommandType;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import io.grpc.stub.ClientResponseObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;

/**
 * @author Taejin Koo
 */
public class GrpcActiveThreadCountService implements ProfilerGrpcCommandService, Closeable {

    private static final long DEFAULT_FLUSH_DELAY = 1000;

    private static final Logger LOGGER = LogManager.getLogger(GrpcActiveThreadCountService.class);
    private final boolean isDebug = LOGGER.isDebugEnabled();

    private final ActiveTraceRepository activeTraceRepository;

    private final GrpcStreamService grpcStreamService = new GrpcStreamService("ActiveThreadCountService", DEFAULT_FLUSH_DELAY);

    public GrpcActiveThreadCountService(ActiveTraceRepository activeTraceRepository) {
        this.activeTraceRepository = Objects.requireNonNull(activeTraceRepository, "activeTraceRepository");
    }

    @Override
    public short getCommandServiceCode() {
        return (short) PCommandType.ACTIVE_THREAD_COUNT.getNumber();
    }

    @Override
    public void handle(PCmdRequest request, ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub) {
        ActiveThreadCountStreamSocket activeThreadCountStreamSocket = new ActiveThreadCountStreamSocket(request.getRequestId(), grpcStreamService);
        ClientResponseObserver<PCmdActiveThreadCountRes, Empty> responseObserver = activeThreadCountStreamSocket.getResponseObserver();
        profilerCommandServiceStub.commandStreamActiveThreadCount(responseObserver);

        grpcStreamService.register(activeThreadCountStreamSocket, new ActiveThreadCountTimerTask());
    }

    private PCmdActiveThreadCountRes.Builder getActiveThreadCountResponse() {
        final long currentTime = System.currentTimeMillis();
        final ActiveTraceHistogram histogram = activeTraceRepository.getActiveTraceHistogram(currentTime);

        PCmdActiveThreadCountRes.Builder responseBuilder = PCmdActiveThreadCountRes.newBuilder();
        responseBuilder.setTimeStamp(currentTime);
        responseBuilder.setHistogramSchemaType(histogram.getHistogramSchema().getTypeCode());

        final List<Integer> activeTraceCountList = histogram.getCounter();
        for (Integer activeTraceCount : activeTraceCountList) {
            responseBuilder.addActiveThreadCount(activeTraceCount);
        }

        return responseBuilder;
    }

    @Override
    public void close() throws IOException {
        LOGGER.info("close");
        grpcStreamService.close();
    }

    private class ActiveThreadCountTimerTask extends TimerTask {

        @Override
        public void run() {
            if (isDebug) {
                LOGGER.debug("ActiveThreadCountTimerTask started. streamSocketList:{}", Arrays.toString(grpcStreamService.getStreamSocketList()));
            }

            PCmdActiveThreadCountRes.Builder activeThreadCountResponseBuilder = getActiveThreadCountResponse();
            for (GrpcProfilerStreamSocket<?, ?> streamSocket : grpcStreamService.getStreamSocketList()) {
                if (streamSocket instanceof ActiveThreadCountStreamSocket) {
                    try {
                        final ActiveThreadCountStreamSocket stream = (ActiveThreadCountStreamSocket) streamSocket;

                        PCmdStreamResponse header = stream.newHeader();
                        activeThreadCountResponseBuilder.setCommonStreamResponse(header);
                        PCmdActiveThreadCountRes activeThreadCount = activeThreadCountResponseBuilder.build();

                        stream.send(activeThreadCount);
                        if (isDebug) {
                            LOGGER.debug("ActiveThreadCountStreamSocket. {}", stream);
                        }
                    } catch (Throwable e) {
                        LOGGER.warn("failed to execute ActiveThreadCountTimerTask.run method. streamSocket:{}, message:{}", streamSocket, e.getMessage(), e);
                        streamSocket.close(e);
                    }
                }
            }
        }

    }

}
