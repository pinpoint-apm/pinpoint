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

import java.util.Objects;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCommandType;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogramUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

/**
 * @author Taejin Koo
 */
public class GrpcActiveThreadCountService implements ProfilerGrpcCommandService, Closeable {

    private static final long DEFAULT_FLUSH_DELAY = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcActiveThreadCountService.class);
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
        profilerCommandServiceStub.commandStreamActiveThreadCount(activeThreadCountStreamSocket.getResponseObserver());

        grpcStreamService.register(activeThreadCountStreamSocket, new ActiveThreadCountTimerTask());
    }

    private PCmdActiveThreadCountRes.Builder getActiveThreadCountResponse() {
        final long currentTime = System.currentTimeMillis();
        final ActiveTraceHistogram histogram = activeTraceRepository.getActiveTraceHistogram(currentTime);

        PCmdActiveThreadCountRes.Builder responseBuilder = PCmdActiveThreadCountRes.newBuilder();
        responseBuilder.setTimeStamp(currentTime);
        responseBuilder.setHistogramSchemaType(histogram.getHistogramSchema().getTypeCode());

        final List<Integer> activeTraceCountList = ActiveTraceHistogramUtils.asList(histogram);
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
                LOGGER.debug("ActiveThreadCountTimerTask started. streamSocketList:{}", grpcStreamService.getStreamSocketList());
            }

            PCmdActiveThreadCountRes.Builder activeThreadCountResponseBuilder = getActiveThreadCountResponse();
            for (GrpcProfilerStreamSocket streamSocket : grpcStreamService.getStreamSocketList()) {
                if (streamSocket != null) {
                    try {
                        streamSocket.send(activeThreadCountResponseBuilder);
                    } catch (Exception e) {
                        LOGGER.warn("failed to execute ActiveThreadCountTimerTask.run method. streamSocket:{}, message:{}", streamSocket, e.getMessage(), e);
                        streamSocket.close(e);
                    }
                }
            }
        }

    }

}
