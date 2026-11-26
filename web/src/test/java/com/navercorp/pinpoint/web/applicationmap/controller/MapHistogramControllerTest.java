package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.bo.ApplicationFactory;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.server.util.UserNodeUtils;
import com.navercorp.pinpoint.common.timeseries.time.Timestamp;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import com.navercorp.pinpoint.web.applicationmap.controller.form.RangeForm;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.service.HistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapHistogramControllerTest {

    @Mock
    ResponseTimeHistogramService responseTimeHistogramService;
    @Mock
    HistogramService histogramService;
    @Mock
    HyperLinkFactory hyperLinkFactory;
    @Mock
    ApplicationFactory applicationFactory;
    @Mock
    ServiceModelResolver serviceModelResolver;
    @Mock
    ServiceTypeRegistryService serviceTypeRegistry;
    @Mock
    ServerMapDataFilter serverMapDataFilter;

    private ApplicationValidator applicationValidator;

    @BeforeEach
    void setup() {
        when(serviceTypeRegistry.findServiceType(ServiceType.STAND_ALONE.getCode())).thenReturn(ServiceType.STAND_ALONE);
        this.applicationValidator = new ApplicationValidator(serviceTypeRegistry);
    }

    private void stubServices(Service... services) {
        for (Service service : services) {
            when(serviceModelResolver.getService(service.getServiceName())).thenReturn(service);
        }
        when(responseTimeHistogramService.selectLinkHistogram(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Application from = invocation.getArgument(0);
                    Application to = invocation.getArgument(1);
                    TimeWindow timeWindow = invocation.getArgument(2);
                    return new LinkHistogramSummary(new Link(LinkDirection.OUT_LINK, new Node(from), new Node(to), timeWindow.getWindowRange()));
                });
    }

    @Test
    void serviceNamesResolveEachEnd() {
        Service fromService = new Service("other", 200);
        Service toService = new Service("another", 300);
        stubServices(fromService, toService);

        newController(null).getLinkHistogram(rangeForm(),
                "other", "was1", 1000,
                "another", "was2", 1000);

        ArgumentCaptor<Application> from = ArgumentCaptor.forClass(Application.class);
        ArgumentCaptor<Application> to = ArgumentCaptor.forClass(Application.class);
        verify(responseTimeHistogramService).selectLinkHistogram(from.capture(), to.capture(), any());
        assertThat(from.getValue().getService()).isEqualTo(fromService);
        assertThat(from.getValue().getApplicationName()).isEqualTo("was1");
        assertThat(to.getValue().getService()).isEqualTo(toService);
        assertThat(to.getValue().getApplicationName()).isEqualTo("was2");
    }

    /**
     * USER -> WAS link whose WAS name is at the V3 limit. The USER name carries the generated suffix and must still pass the validator.
     */
    @Test
    void userNodeNameWithSuffix_atMaxWasNameLength_isOk() {
        ServiceType was = ServiceType.STAND_ALONE;
        ServiceType user = ServiceType.USER;
        when(serviceTypeRegistry.findServiceType(user.getCode())).thenReturn(user);
        stubServices(new Service("svc", 100));

        String maxWasName = "a".repeat(PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
        String userNodeName = UserNodeUtils.newUserNodeName(maxWasName, was);

        newController(null).getLinkHistogram(rangeForm(),
                "svc", userNodeName, user.getCode(),
                "svc", maxWasName, was.getCode());

        ArgumentCaptor<Application> from = ArgumentCaptor.forClass(Application.class);
        verify(responseTimeHistogramService).selectLinkHistogram(from.capture(), any(), any());
        assertThat(from.getValue().getApplicationName()).isEqualTo(userNodeName);
        assertThat(from.getValue().getServiceType()).isEqualTo(user);
    }

    @Test
    void noFilter_skipsAuthorization() {
        stubServices(new Service("svc", 100));

        newController(null).getLinkHistogram(rangeForm(),
                "svc", "was1", 1000,
                "svc", "was2", 1000);

        verify(responseTimeHistogramService).selectLinkHistogram(any(), any(), any());
    }

    @Test
    void rejectedLink_isForbidden() {
        Service service = new Service("svc", 100);
        stubServices(service);
        MapHistogramController controller = newController(serverMapDataFilter);
        Application from = new Application(service, "was1", ServiceType.STAND_ALONE);
        Application to = new Application(service, "was2", ServiceType.STAND_ALONE);

        when(serverMapDataFilter.filterLink(from, to)).thenReturn(true);
        assertForbidden(() -> controller.getLinkHistogram(rangeForm(),
                "svc", "was1", 1000,
                "svc", "was2", 1000));
        verify(responseTimeHistogramService, never()).selectLinkHistogram(any(), any(), any());

        when(serverMapDataFilter.filterLink(from, to)).thenReturn(false);
        controller.getLinkHistogram(rangeForm(),
                "svc", "was1", 1000,
                "svc", "was2", 1000);
        verify(responseTimeHistogramService).selectLinkHistogram(eq(from), eq(to), any());
    }

    private void assertForbidden(Executable executable) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, executable);
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private MapHistogramController newController(ServerMapDataFilter filter) {
        return new MapHistogramController(
                responseTimeHistogramService, histogramService, applicationFactory,
                applicationValidator, serviceModelResolver, hyperLinkFactory, filter, Duration.ofDays(2));
    }

    private RangeForm rangeForm() {
        return new RangeForm(Timestamp.ofEpochMilli(0), Timestamp.ofEpochMilli(60000));
    }
}
