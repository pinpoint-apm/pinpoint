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
import com.navercorp.pinpoint.profiler.context.grpc.GrpcThreadStateMessageConverter;
import com.navercorp.pinpoint.profiler.receiver.ProfilerSimpleCommandService;
import com.navercorp.pinpoint.profiler.receiver.service.ActiveThreadDumpCoreService;
import com.navercorp.pinpoint.profiler.receiver.service.ThreadDump;
import com.navercorp.pinpoint.profiler.receiver.service.ThreadDumpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class GrpcActiveThreadLightDumpService implements ProfilerSimpleCommandService<PCmdRequest> {

    static final String JAVA = "JAVA";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GrpcThreadStateMessageConverter grpcThreadStateMessageConverter = new GrpcThreadStateMessageConverter();

    private final ActiveThreadDumpCoreService activeThreadDump;

    private final ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub;

    public GrpcActiveThreadLightDumpService(ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub, ActiveTraceRepository activeTraceRepository) {
        this.profilerCommandServiceStub = Assert.requireNonNull(profilerCommandServiceStub, "profilerCommandServiceStub");
        Assert.requireNonNull(activeTraceRepository, "activeTraceRepository");

        this.activeThreadDump = new ActiveThreadDumpCoreService(activeTraceRepository);
    }

    @Override
    public void simpleCommandService(PCmdRequest request) {
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

        profilerCommandServiceStub.commandActiveThreadLightDump(builder.build(), EmptyStreamObserver.create());
    }

    private List<PActiveThreadLightDump> getActiveThreadDumpList(PCmdActiveThreadLightDump commandActiveThreadLightDump) {
        ThreadDumpRequest request = ThreadDumpRequest.create(commandActiveThreadLightDump);

        Collection<ThreadDump> activeTraceInfoList = activeThreadDump.getActiveThreadDumpList(request);

        return toPActiveThreadLightDump(activeTraceInfoList);
    }

    private List<PActiveThreadLightDump> toPActiveThreadLightDump(Collection<ThreadDump> activeTraceInfoList) {
        final List<PActiveThreadLightDump> result = new ArrayList<PActiveThreadLightDump>(activeTraceInfoList.size());
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
        builder.setThreadState(grpcThreadStateMessageConverter.toMessage(threadInfo.getThreadState()));
        return builder.build();
    }

    @Override
    public short getCommandServiceCode() {
        return PCommandType.ACTIVE_THREAD_LIGHT_DUMP_VALUE;
    }

}

