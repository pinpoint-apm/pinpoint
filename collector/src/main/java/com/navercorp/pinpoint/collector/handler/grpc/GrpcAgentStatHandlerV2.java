/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.handler.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.io.request.ServerRequest;
import io.grpc.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
@Service
public class GrpcAgentStatHandlerV2 implements SimpleHandler<GeneratedMessageV3> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final GrpcMetricHandler[] metricHandlers;


    public GrpcAgentStatHandlerV2(List<GrpcMetricHandler> metricHandlers) {
        Objects.requireNonNull(metricHandlers, "metricHandlers");
        this.metricHandlers = metricHandlers.toArray(new GrpcMetricHandler[]{});

        for (GrpcMetricHandler handler : this.metricHandlers) {
            logger.info("{}:{}", GrpcMetricHandler.class.getSimpleName(), handler);
        }
    }

    @Override
    public void handleSimple(ServerRequest<GeneratedMessageV3> serverRequest) {
        final GeneratedMessageV3 data = serverRequest.getData();

        for (GrpcMetricHandler messageHandler : metricHandlers) {
            if (messageHandler.accept(data)) {
                messageHandler.handle(data);
                return;
            }
        }

        logger.warn("Invalid request type. serverRequest={}", serverRequest);
        throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
    }

}