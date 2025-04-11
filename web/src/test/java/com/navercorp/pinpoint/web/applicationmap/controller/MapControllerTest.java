package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.service.MapService;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
class MapControllerTest {

    @Mock
    MapService mapService;
    @Mock
    ApplicationValidator applicationValidator;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        Duration duration = Duration.ofMinutes(1);
        MapController controller = new MapController(mapService, applicationValidator, duration);
        when(applicationValidator.newApplication(any(), anyInt(), any()))
                .thenReturn(new Application("test", ServiceType.STAND_ALONE));

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getServerMapData() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/servermap/getServerMapDataV2")
                        .param("applicationName", "testApp")
//                        .param("serviceTypeCode", String.valueOf(123))
                        .param("serviceTypeName", ServiceType.STAND_ALONE.getName())
                        .param("from", "0")
                        .param("to", "10000")
                )
                .andDo(print());
//                .andExpect(status().isOk());

        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }
}