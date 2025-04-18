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
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.grpc.trace.PActiveThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.grpc.trace.PCommandType;
import com.navercorp.pinpoint.grpc.trace.PThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ThreadDumpMapper;
import com.navercorp.pinpoint.profiler.receiver.service.ActiveThreadDumpCoreService;
import com.navercorp.pinpoint.profiler.receiver.service.ThreadDump;
import com.navercorp.pinpoint.profiler.receiver.service.ThreadDumpRequest;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class GrpcActiveThreadLightDumpService implements ProfilerGrpcCommandService {

    static final String JAVA = "JAVA";

    private final Logger logger = LogManager.getLogger(this.getClass());


    private final ThreadDumpMapper mapper;

    private final ActiveThreadDumpCoreService activeThreadDump;

    public GrpcActiveThreadLightDumpService(
            ActiveTraceRepository activeTraceRepository,
            ThreadDumpMapper threadDumpMapper
    ) {
        Objects.requireNonNull(activeTraceRepository, "activeTraceRepository");

        this.activeThreadDump = new ActiveThreadDumpCoreService(activeTraceRepository);
        this.mapper = Objects.requireNonNull(threadDumpMapper, "threadDumpMapper");
    }

    @Override
    public void handle(PCmdRequest request, ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub) {
        logger.info("simpleCommandService:{}", request);

        PCmdActiveThreadLightDump commandActiveThreadLightDump = request.getCommandActiveThreadLightDump();

        PCmdActiveThreadLightDumpRes.Builder builder = PCmdActiveThreadLightDumpRes.newBuilder();

        PCmdResponse commonResponse = PCmdResponse.newBuilder().setResponseId(request.getRequestId()).build();
        builder.setCommonResponse(commonResponse);

        builder.setType(JAVA);
        builder.setSubType(JvmUtils.getType().name());
        builder.setVersion(JvmUtils.getVersion().name());

        List<PActiveThreadLightDump> activeThreadDumpList = getActiveThreadDumpList(commandActiveThreadLightDump);
        builder.addAllThreadDump(activeThreadDumpList);
        PCmdActiveThreadLightDumpRes activeThreadLightDump = builder.build();

        StreamObserver<Empty> response = ResponseStreamObserver.responseStream("ActiveThreadLightDumpResponse");
        profilerCommandServiceStub.commandActiveThreadLightDump(activeThreadLightDump, response);
    }

    private List<PActiveThreadLightDump> getActiveThreadDumpList(PCmdActiveThreadLightDump commandActiveThreadLightDump) {
        ThreadDumpRequest request = ThreadDumpRequest.create(commandActiveThreadLightDump);

        Collection<ThreadDump> activeTraceInfoList = activeThreadDump.getActiveThreadDumpList(request);

        return toPActiveThreadLightDump(activeTraceInfoList);
    }

    private List<PActiveThreadLightDump> toPActiveThreadLightDump(Collection<ThreadDump> activeTraceInfoList) {
        final List<PActiveThreadLightDump> result = new ArrayList<>(activeTraceInfoList.size());
        for (ThreadDump threadDump : activeTraceInfoList) {
            PActiveThreadLightDump pActiveThreadLightDump = createActiveThreadDump(threadDump);
            result.add(pActiveThreadLightDump);
        }

        return result;
    }

    private PActiveThreadLightDump createActiveThreadDump(ThreadDump threadDump) {
        final ActiveTraceSnapshot activeTraceInfo = threadDump.getActiveTraceSnapshot();
        final ThreadInfo threadInfo = threadDump.getThreadInfo();

        PThreadLightDump pThreadLightDump = createPThreadLightDump(threadInfo);

        PActiveThreadLightDump.Builder builder = PActiveThreadLightDump.newBuilder();
        builder.setStartTime(activeTraceInfo.getStartTime());
        builder.setLocalTraceId(activeTraceInfo.getLocalTransactionId());
        builder.setThreadDump(pThreadLightDump);

        if (activeTraceInfo.isSampled()) {
            builder.setSampled(true);
            builder.setTransactionId(activeTraceInfo.getTransactionId());
            builder.setEntryPoint(activeTraceInfo.getEntryPoint());
        }
        return builder.build();
    }

    private PThreadLightDump createPThreadLightDump(ThreadInfo threadInfo) {
        PThreadLightDump.Builder builder = PThreadLightDump.newBuilder();
        builder.setThreadName(threadInfo.getThreadName());
        builder.setThreadId(threadInfo.getThreadId());
        builder.setThreadState(mapper.map(threadInfo.getThreadState()));
        return builder.build();
    }

    @Override
    public short getCommandServiceCode() {
        return PCommandType.ACTIVE_THREAD_LIGHT_DUMP_VALUE;
    }

}

