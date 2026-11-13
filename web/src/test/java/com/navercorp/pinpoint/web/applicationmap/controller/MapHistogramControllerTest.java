package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.bo.ApplicationFactory;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import com.navercorp.pinpoint.service.web.resolver.ServiceNameExtractor;
import com.navercorp.pinpoint.service.web.resolver.ServiceParamArgumentResolver;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.service.HistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MapHistogramControllerTest {

    private static final String SERVICE_HEADER = "X-Service-Name";

    @Mock
    ResponseTimeHistogramService responseTimeHistogramService;
    @Mock
    HistogramService histogramService;
    @Mock
    ApplicationValidator applicationValidator;
    @Mock
    HyperLinkFactory hyperLinkFactory;
    @Mock
    ApplicationFactory applicationFactory;
    @Mock
    ServiceModelResolver serviceModelResolver;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MapHistogramController controller = new MapHistogramController(
                responseTimeHistogramService, histogramService, applicationFactory,
                applicationValidator, serviceModelResolver, hyperLinkFactory, Duration.ofDays(2));

        ServiceNameExtractor headerExtractor = request -> request.getHeader(SERVICE_HEADER);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new ServiceParamArgumentResolver(List.of(headerExtractor)))
                .build();
    }

    private void stubServices(Service headerService, Service otherService) {
        when(serviceModelResolver.getService(headerService.getServiceName())).thenReturn(headerService);
        if (otherService != null) {
            when(serviceModelResolver.getService(otherService.getServiceName())).thenReturn(otherService);
        }
        when(applicationValidator.newApplication(any(Service.class), anyString(), anyInt(), any()))
                .thenAnswer(invocation -> new Application(invocation.getArgument(0), invocation.getArgument(1), ServiceType.STAND_ALONE));
        when(responseTimeHistogramService.selectLinkHistogram(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Application from = invocation.getArgument(0);
                    Application to = invocation.getArgument(1);
                    TimeWindow timeWindow = invocation.getArgument(2);
                    return new LinkHistogramSummary(new Link(LinkDirection.OUT_LINK, new Node(from), new Node(to), timeWindow.getWindowRange()));
                });
    }

    @Test
    void missingToApplicationName_isBadRequest() throws Exception {
        mockMvc.perform(get("/api/servermap/linkHistogram")
                        .header(SERVICE_HEADER, "svc")
                        .param("from", "0")
                        .param("to", "60000")
                        .param("fromApplicationName", "was1")
                        .param("fromServiceTypeCode", "1000")
                        .param("toServiceTypeCode", "1000"))
                .andExpect(status().isBadRequest());

        verify(responseTimeHistogramService, never()).selectLinkHistogram(any(), any(), any());
    }

    @Test
    void serviceNamesDefaultToHeaderService() throws Exception {
        Service headerService = new Service("svc", 100);
        stubServices(headerService, null);

        mockMvc.perform(get("/api/servermap/linkHistogram")
                        .header(SERVICE_HEADER, "svc")
                        .param("from", "0")
                        .param("to", "60000")
                        .param("fromApplicationName", "was1")
                        .param("fromServiceTypeCode", "1000")
                        .param("toApplicationName", "was2")
                        .param("toServiceTypeCode", "1000"))
                .andExpect(status().isOk());

        ArgumentCaptor<Application> from = ArgumentCaptor.forClass(Application.class);
        ArgumentCaptor<Application> to = ArgumentCaptor.forClass(Application.class);
        verify(responseTimeHistogramService).selectLinkHistogram(from.capture(), to.capture(), any());
        assertThat(from.getValue().getService()).isEqualTo(headerService);
        assertThat(from.getValue().getApplicationName()).isEqualTo("was1");
        assertThat(to.getValue().getService()).isEqualTo(headerService);
        assertThat(to.getValue().getApplicationName()).isEqualTo("was2");
    }

    @Test
    void explicitServiceNameOverridesHeaderService() throws Exception {
        Service headerService = new Service("svc", 100);
        Service otherService = new Service("other", 200);
        stubServices(headerService, otherService);

        mockMvc.perform(get("/api/servermap/linkHistogram")
                        .header(SERVICE_HEADER, "svc")
                        .param("from", "0")
                        .param("to", "60000")
                        .param("fromServiceName", "other")
                        .param("fromApplicationName", "was1")
                        .param("fromServiceTypeCode", "1000")
                        .param("toApplicationName", "was2")
                        .param("toServiceTypeCode", "1000"))
                .andExpect(status().isOk());

        ArgumentCaptor<Application> from = ArgumentCaptor.forClass(Application.class);
        ArgumentCaptor<Application> to = ArgumentCaptor.forClass(Application.class);
        verify(responseTimeHistogramService).selectLinkHistogram(from.capture(), to.capture(), any());
        assertThat(from.getValue().getService()).isEqualTo(otherService);
        assertThat(to.getValue().getService()).isEqualTo(headerService);
        verify(applicationValidator).newApplication(eq(otherService), eq("was1"), eq(1000), any());
    }
}
