package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.timeseries.time.Timestamp;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.service.web.vo.ServiceName;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.service.ApdexScoreService;
import com.navercorp.pinpoint.web.service.ServiceModelResolver;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApdexScoreControllerTest {

    private static final Timestamp FROM = Timestamp.ofEpochMilli(0);
    private static final Timestamp TO = Timestamp.ofEpochMilli(300000);
    private static final Service SERVICE = new Service("my-service", 123);
    private static final Application APPLICATION = new Application(SERVICE, "testApp", ServiceType.STAND_ALONE);

    @Mock
    private ApplicationFactory applicationFactory;
    @Mock
    private ApdexScoreService apdexScoreService;
    @Mock
    private ServiceModelResolver serviceModelResolver;

    private ApdexScoreController controller;

    @BeforeEach
    void setUp() {
        controller = new ApdexScoreController(applicationFactory, apdexScoreService, serviceModelResolver);
        when(serviceModelResolver.getService("my-service")).thenReturn(SERVICE);
    }

    @Test
    void getApdexScoreByServiceTypeCode() {
        when(applicationFactory.createApplication(SERVICE, "testApp", 1400)).thenReturn(APPLICATION);

        controller.getApdexScore(new ServiceName("my-service"), "testApp", (short) 1400, FROM, TO);

        verify(apdexScoreService).selectApdexScoreData(eq(APPLICATION), any(TimeWindow.class));
    }

    @Test
    void getApdexScoreByServiceTypeName() {
        when(applicationFactory.createApplicationByTypeName(SERVICE, "testApp", "SPRING_BOOT")).thenReturn(APPLICATION);

        controller.getApdexScore(new ServiceName("my-service"), "testApp", "SPRING_BOOT", FROM, TO);

        verify(apdexScoreService).selectApdexScoreData(eq(APPLICATION), any(TimeWindow.class));
    }

    @Test
    void getAgentApdexScoreByServiceTypeCode() {
        when(applicationFactory.createApplication(SERVICE, "testApp", 1400)).thenReturn(APPLICATION);

        controller.getApdexScore(new ServiceName("my-service"), "testApp", (short) 1400, "agent-01", FROM, TO);

        verify(apdexScoreService).selectApdexScoreData(eq(APPLICATION), eq("agent-01"), any(TimeWindow.class));
    }

    @Test
    void getAgentApdexScoreByServiceTypeName() {
        when(applicationFactory.createApplicationByTypeName(SERVICE, "testApp", "SPRING_BOOT")).thenReturn(APPLICATION);

        controller.getApdexScore(new ServiceName("my-service"), "testApp", "SPRING_BOOT", "agent-01", FROM, TO);

        verify(apdexScoreService).selectApdexScoreData(eq(APPLICATION), eq("agent-01"), any(TimeWindow.class));
    }

    @Test
    void getApplicationApdexScoreChartByServiceTypeCode() {
        when(applicationFactory.createApplication(SERVICE, "testApp", 1400)).thenReturn(APPLICATION);

        controller.getApplicationApdexScoreChart(new ServiceName("my-service"), "testApp", (short) 1400, FROM, TO);

        verify(apdexScoreService).selectApplicationChart(eq(APPLICATION), any(TimeWindow.class));
    }

    @Test
    void getApplicationApdexScoreChartByServiceTypeName() {
        when(applicationFactory.createApplicationByTypeName(SERVICE, "testApp", "SPRING_BOOT")).thenReturn(APPLICATION);

        controller.getApplicationApdexScoreChart(new ServiceName("my-service"), "testApp", "SPRING_BOOT", FROM, TO);

        verify(apdexScoreService).selectApplicationChart(eq(APPLICATION), any(TimeWindow.class));
    }

    @Test
    void getAgentApdexScoreChartByServiceTypeCode() {
        when(applicationFactory.createApplication(SERVICE, "testApp", 1400)).thenReturn(APPLICATION);

        controller.getAgentApdexScoreChart(new ServiceName("my-service"), "testApp", (short) 1400, "agent-01", FROM, TO);

        verify(apdexScoreService).selectAgentChart(eq(APPLICATION), any(TimeWindow.class), eq("agent-01"));
    }

    @Test
    void getAgentApdexScoreChartByServiceTypeName() {
        when(applicationFactory.createApplicationByTypeName(SERVICE, "testApp", "SPRING_BOOT")).thenReturn(APPLICATION);

        controller.getAgentApdexScoreChart(new ServiceName("my-service"), "testApp", "SPRING_BOOT", "agent-01", FROM, TO);

        verify(apdexScoreService).selectAgentChart(eq(APPLICATION), any(TimeWindow.class), eq("agent-01"));
    }
}
