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

import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCommandType;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class GrpcActiveThreadCountService implements ProfilerGrpcCommandService, Closeable {

    private final Logger logger = LogManager.getLogger(getClass());
    private final AtomicInteger sequence = new AtomicInteger(0);

    private final GrpcStreamService grpcStreamService ;

    public GrpcActiveThreadCountService(GrpcStreamService grpcStreamService) {
        this.grpcStreamService = Objects.requireNonNull(grpcStreamService, "grpcStreamService");
    }

    @Override
    public short getCommandServiceCode() {
        return (short) PCommandType.ACTIVE_THREAD_COUNT.getNumber();
    }

    @Override
    public void handle(PCmdRequest request, ProfilerCommandServiceGrpc.ProfilerCommandServiceStub commandServiceStub) {
        ActiveThreadCountStreamSocket socket = new ActiveThreadCountStreamSocket(sequence.getAndIncrement(), request.getRequestId(), grpcStreamService);
        commandServiceStub.commandStreamActiveThreadCount(socket);

        grpcStreamService.register(socket);
    }


    @Override
    public void close() throws IOException {
        logger.info("close");
        grpcStreamService.close();
    }

}
