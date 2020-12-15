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

import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatBatchMapper;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentStatMapper;
import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcAgentUriStatMapper;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.collector.service.AgentUriStatService;
import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.grpc.trace.PEachUriStat;
import com.navercorp.pinpoint.grpc.trace.PUriHistogram;
import com.navercorp.pinpoint.io.request.ServerRequest;

import io.grpc.Context;
import io.grpc.StatusRuntimeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.times;

/**
 * @author Taejin Koo
 */
public class GrpcAgentUriStatHandlerV2Test {

    private Context prevContext;

    @Before
    public void setUp() throws Exception {
        Context root = Context.ROOT;
        prevContext = root.attach();
    }

    @After
    public void tearDown() throws Exception {
        Context root = Context.ROOT;
        if (prevContext != null) {
            root.detach(prevContext);
        }
    }

    @Test(expected = StatusRuntimeException.class)
    public void throwExceptionTest() {
        AgentUriStatService mockAgentUriStatService = Mockito.mock(AgentUriStatService.class);

        ServerRequest mockServerRequest = Mockito.mock(ServerRequest.class);

        GrpcAgentStatHandlerV2 handler = createMockHandler(mockAgentUriStatService, false);

        handler.handleSimple(mockServerRequest);
    }

    @Test
    public void skipTest() {
        AgentUriStatService mockAgentUriStatService = Mockito.mock(AgentUriStatService.class);

        ServerRequest mockServerRequest = Mockito.mock(ServerRequest.class);
        Mockito.when(mockServerRequest.getData()).thenReturn(PAgentUriStat.getDefaultInstance());

        GrpcAgentStatHandlerV2 handler = createMockHandler(mockAgentUriStatService, false);
        handler.handleSimple(mockServerRequest);

        Mockito.verify(mockAgentUriStatService, times(0)).save(Mockito.any());
    }

    @Test
    public void handleTest() {
        AgentUriStatService mockAgentUriStatService = Mockito.mock(AgentUriStatService.class);

        attachContext(new Header("agent", "applicationName", System.currentTimeMillis(), Header.SOCKET_ID_NOT_EXIST, new ArrayList<>()));

        PAgentUriStat pAgentUriStat = createPAgentUriStat();

        ServerRequest mockServerRequest = Mockito.mock(ServerRequest.class);
        Mockito.when(mockServerRequest.getData()).thenReturn(pAgentUriStat);

        GrpcAgentStatHandlerV2 handler = createMockHandler(mockAgentUriStatService, true);
        handler.handleSimple(mockServerRequest);

        Mockito.verify(mockAgentUriStatService, times(1)).save(Mockito.any());
    }

    private PAgentUriStat createPAgentUriStat() {
        PUriHistogram.Builder histogramBuilder = PUriHistogram.newBuilder();

        int totalCount = 0;
        for (int i = 0; i < UriStatHistogramBucket.values().length; i++) {
            int count = ThreadLocalRandom.current().nextInt(0, 10);
            totalCount += count;

            histogramBuilder.addHistogram(count);
        }
        histogramBuilder.setCount(totalCount);

        PEachUriStat.Builder eachUriStatBuilder = PEachUriStat.newBuilder();
        eachUriStatBuilder.setUri("uri");
        eachUriStatBuilder.setTotalHistogram(histogramBuilder.build());

        PAgentUriStat.Builder builder = PAgentUriStat.newBuilder();
        builder.setBucketVersion(0);
        builder.setTimestamp(System.currentTimeMillis());
        builder.addEachUriStat(eachUriStatBuilder.build());

        return builder.build();
    }

    private void attachContext(Header header) {
        final Context currentContext = Context.current();
        Context newContext = currentContext.withValue(ServerContext.getAgentInfoKey(), header);
        newContext.attach();
    }


    private GrpcAgentStatHandlerV2 createMockHandler(AgentUriStatService agentUriStatService, boolean enableUriStat) {
        GrpcAgentStatMapper mockAgentStatMapper = Mockito.mock(GrpcAgentStatMapper.class);
        GrpcAgentStatBatchMapper agentStatBatchMapper = new GrpcAgentStatBatchMapper(mockAgentStatMapper);
        List<AgentStatService> agentStatServiceList = new ArrayList<>();
        Optional<List<AgentStatService>> agentStatServices = Optional.of(agentStatServiceList);

        CollectorConfiguration collectorConfiguration = Mockito.mock(CollectorConfiguration.class);
        Mockito.when(collectorConfiguration.isUriStatEnable()).thenReturn(enableUriStat);

        return new GrpcAgentStatHandlerV2(mockAgentStatMapper, agentStatBatchMapper, new GrpcAgentUriStatMapper(),
                agentStatServices, agentUriStatService, collectorConfiguration);
    }

}
