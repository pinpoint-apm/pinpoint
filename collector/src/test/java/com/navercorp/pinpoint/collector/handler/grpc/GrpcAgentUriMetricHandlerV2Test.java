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

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.handler.grpc.metric.AgentUriMetricHandler;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentUriStatMapper;
import com.navercorp.pinpoint.collector.service.AgentUriStatService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderV1;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PEachUriStat;
import com.navercorp.pinpoint.grpc.trace.PUriHistogram;
import com.navercorp.pinpoint.io.request.GrpcServerHeaderV1;
import com.navercorp.pinpoint.io.request.ServerHeader;
import com.navercorp.pinpoint.io.request.ServerRequest;
import io.grpc.Context;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class GrpcAgentUriMetricHandlerV2Test {


    @SuppressWarnings("unchecked")
    private <T> ServerRequest<T> serverRequestMock() {
        return (ServerRequest<T>) mock(ServerRequest.class);
    }

    @Test
    public void handleTest() {
        AgentUriStatService mockAgentUriStatService = mock(AgentUriStatService.class);

        PAgentUriStat pAgentUriStat = createPAgentUriStat();

        ServerRequest<PAgentUriStat> mockServerRequest = serverRequestMock();
        Header header = HeaderV1.simple("name", "agentId", "agentName", "applicationName",
                ServiceType.UNKNOWN.getCode(), 0);
        ServerHeader serverHeader = new GrpcServerHeaderV1(header);
        when(mockServerRequest.getHeader()).thenReturn(serverHeader);
        when(mockServerRequest.getData()).thenReturn(pAgentUriStat);

        SimpleHandler<PAgentUriStat> handler = createMockHandler(mockAgentUriStatService);
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


    private SimpleHandler<PAgentUriStat> createMockHandler(AgentUriStatService agentUriStatService) {

        GrpcAgentUriStatMapper grpcAgentUriStatMapper = new GrpcAgentUriStatMapper();
        return new AgentUriMetricHandler(grpcAgentUriStatMapper, agentUriStatService);
    }

}
