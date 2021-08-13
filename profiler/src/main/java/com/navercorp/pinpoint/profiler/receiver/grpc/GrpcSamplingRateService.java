/*
 * Copyright 2021 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdSamplingRate;
import com.navercorp.pinpoint.grpc.trace.PCmdSamplingRateResponse;
import com.navercorp.pinpoint.grpc.trace.PCommandType;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yjqg6666
 */
public class GrpcSamplingRateService implements ProfilerGrpcCommandService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Sampler sampler;

    public GrpcSamplingRateService(Sampler sampler) {
        this.sampler = sampler;
    }

    @Override
    public void handle(PCmdRequest request, ProfilerCommandServiceGrpc.ProfilerCommandServiceStub profilerCommandServiceStub) {
        logger.info("simpleCommandService:{}", request);

        PCmdSamplingRate commandSamplingRate = request.getCommandSamplingRate();
        final double requestSamplingRate = commandSamplingRate.getSamplingRate();
        if (requestSamplingRate >= 0) {
            sampler.updateSamplingRate(requestSamplingRate);
        }
        PCmdSamplingRateResponse.Builder responseBuilder = PCmdSamplingRateResponse.newBuilder();
        responseBuilder.setSamplingRate(sampler.getSamplingRate());

        PCmdResponse commonResponse = PCmdResponse.newBuilder().setResponseId(request.getRequestId()).build();
        responseBuilder.setCommonResponse(commonResponse);

        profilerCommandServiceStub.commandSamplingRate(responseBuilder.build(), EmptyStreamObserver.create());
    }

    @Override
    public short getCommandServiceCode() {
        return PCommandType.SAMPLING_RATE_VALUE;
    }

}
