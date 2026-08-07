package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.mapper.grpc.GrpcAgentInfoBoMapper;
import com.navercorp.pinpoint.collector.service.AgentInfoService;
import com.navercorp.pinpoint.collector.service.AgentInfoStatisticsService;
import com.navercorp.pinpoint.collector.service.ApplicationIndexV2Service;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.io.ServerRequest;
import com.navercorp.pinpoint.common.server.io.ServerResponse;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderV1;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.GrpcServerHeaderV1;
import com.navercorp.pinpoint.io.request.UidFetcher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class GrpcAgentInfoHandlerTest {

    @Test
    public void serviceNotFound() {
        AgentInfoService agentInfoService = mock(AgentInfoService.class);
        ApplicationIndexV2Service applicationIndexV2Service = mock(ApplicationIndexV2Service.class);
        AgentInfoStatisticsService agentInfoStatisticsService = mock(AgentInfoStatisticsService.class);
        GrpcAgentInfoBoMapper mapper = mock(GrpcAgentInfoBoMapper.class);
        GrpcAgentInfoHandler handler = new GrpcAgentInfoHandler(agentInfoService, applicationIndexV2Service, agentInfoStatisticsService, mapper);

        Header header = HeaderV1.simple("name", "agentId", "agentName", "applicationName", 0, 1668495162817L);
        UidFetcher notFoundFetcher = serviceName -> CompletableFuture.completedFuture(null);
        ServerHeader serverHeader = new GrpcServerHeaderV1(header, notFoundFetcher);

        @SuppressWarnings("unchecked")
        ServerRequest<PAgentInfo> serverRequest = mock(ServerRequest.class);
        when(serverRequest.getData()).thenReturn(PAgentInfo.getDefaultInstance());
        when(serverRequest.getHeader()).thenReturn(serverHeader);

        @SuppressWarnings("unchecked")
        ServerResponse<PResult> serverResponse = mock(ServerResponse.class);

        handler.handleRequest(serverRequest, serverResponse);

        ArgumentCaptor<PResult> captor = ArgumentCaptor.forClass(PResult.class);
        verify(serverResponse).write(captor.capture());
        PResult result = captor.getValue();
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Service not found");
        verifyNoInteractions(agentInfoService);
    }
}