/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.handler.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.collector.handler.grpc.metric.AgentMetricBatchHandler;
import com.navercorp.pinpoint.collector.handler.grpc.metric.AgentMetricHandler;
import com.navercorp.pinpoint.collector.handler.grpc.metric.AgentUriMetricHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatBatchMapper;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatMapper;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentUriStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.collector.service.AgentUriStatService;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PEachUriStat;
import com.navercorp.pinpoint.grpc.trace.PUriHistogram;
import com.navercorp.pinpoint.io.request.ServerRequest;
import io.grpc.Context;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class GrpcAgentUriMetricHandlerV2Test {

    private Context prevContext;

    @BeforeEach
    public void setUp() {
        Context root = Context.ROOT;
        prevContext = root.attach();
    }

    @AfterEach
    public void tearDown() {
        Context root = Context.ROOT;
        if (prevContext != null) {
            root.detach(prevContext);
        }
    }

    @Test
    public void throwExceptionTest() {
        Assertions.assertThrows(StatusRuntimeException.class, () -> {
            AgentUriStatService mockAgentUriStatService = mock(AgentUriStatService.class);
            ServerRequest<GeneratedMessageV3> mockServerRequest = mock(ServerRequest.class);

            GrpcAgentStatHandlerV2 handler = createMockHandler(mockAgentUriStatService, false);

            handler.handleSimple(mockServerRequest);
        });
    }

    @Test
    public void skipTest() {
        AgentUriStatService mockAgentUriStatService = mock(AgentUriStatService.class);
        ServerRequest<GeneratedMessageV3> mockServerRequest = mock(ServerRequest.class);
        when(mockServerRequest.getData()).thenReturn(PAgentUriStat.getDefaultInstance());

        GrpcAgentStatHandlerV2 handler = createMockHandler(mockAgentUriStatService, false);
        handler.handleSimple(mockServerRequest);
    }

    @Test
    public void handleTest() {
        AgentUriStatService mockAgentUriStatService = mock(AgentUriStatService.class);

        attachContext(new Header("name", AgentId.of("agentId"), "agentName", "applicationName", "serviceName",
                ServiceType.UNKNOWN.getCode(), System.currentTimeMillis(), Header.SOCKET_ID_NOT_EXIST, new ArrayList<>()));

        PAgentUriStat pAgentUriStat = createPAgentUriStat();

        ServerRequest<GeneratedMessageV3> mockServerRequest = mock(ServerRequest.class);
        when(mockServerRequest.getData()).thenReturn(pAgentUriStat);

        GrpcAgentStatHandlerV2 handler = createMockHandler(mockAgentUriStatService, true);
        handler.handleSimple(mockServerRequest);

    }

    private PAgentUriStat createPAgentUriStat() {
        PUriHistogram.Builder histogramBuilder = PUriHistogram.newBuilder();

        for (int i = 0; i < UriStatHistogramBucket.getLayout().getBucketSize(); i++) {
            int count = ThreadLocalRandom.current().nextInt(0, 10);
            histogramBuilder.addHistogram(count);
        }

        PEachUriStat.Builder eachUriStatBuilder = PEachUriStat.newBuilder();
        eachUriStatBuilder.setUri("uri");
        eachUriStatBuilder.setTotalHistogram(histogramBuilder.build());
        eachUriStatBuilder.setTimestamp(System.currentTimeMillis());

        PAgentUriStat.Builder builder = PAgentUriStat.newBuilder();
        builder.setBucketVersion(0);
        builder.addEachUriStat(eachUriStatBuilder.build());

        return builder.build();
    }

    private void attachContext(Header header) {
        final Context currentContext = Context.current();
        Context newContext = currentContext.withValue(ServerContext.getAgentInfoKey(), header);
        newContext.attach();
    }


    private GrpcAgentStatHandlerV2 createMockHandler(AgentUriStatService agentUriStatService, boolean enableUriStat) {
        GrpcAgentStatMapper mockAgentStatMapper = mock(GrpcAgentStatMapper.class);
        GrpcAgentStatBatchMapper agentStatBatchMapper = new GrpcAgentStatBatchMapper(mockAgentStatMapper);

        AgentStatService[] agentStatServices = new AgentStatService[0];

        AgentMetricHandler statHandler = new AgentMetricHandler(mockAgentStatMapper, agentStatServices);
        AgentMetricBatchHandler statBatchHandler = new AgentMetricBatchHandler(agentStatBatchMapper, statHandler);


        CollectorProperties collectorProperties = mock(CollectorProperties.class);
        when(collectorProperties.isUriStatEnable()).thenReturn(enableUriStat);
        GrpcAgentUriStatMapper grpcAgentUriStatMapper = new GrpcAgentUriStatMapper();
        AgentUriMetricHandler uriHandler = new AgentUriMetricHandler(collectorProperties, grpcAgentUriStatMapper, agentUriStatService);

        List<GrpcMetricHandler> handlers = List.of(statHandler, statBatchHandler, uriHandler);

        return new GrpcAgentStatHandlerV2(handlers);
    }

}
