/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import com.navercorp.pinpoint.service.web.resolver.DefaultServiceNameExtractor;
import com.navercorp.pinpoint.service.web.resolver.ServiceParamArgumentResolver;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.SimpleApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.config.MapProperties;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelectorType;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.service.MapService;
import com.navercorp.pinpoint.web.applicationmap.service.MapServiceOption;
import com.navercorp.pinpoint.web.service.CommonService;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MapControllerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Mock
    MapService mapService;
    @Mock
    ApplicationValidator applicationValidator;
    @Mock
    ServiceModelResolver serviceModelResolver;
    @Mock
    CommonService commonService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        ApplicationMap map = new SimpleApplicationMap(NodeList.EMPTY, LinkList.EMPTY, Range.between(0, 10000));
        when(mapService.selectApplicationMap(any())).thenReturn(map);

        MapProperties mapProperties = mock(MapProperties.class);
        lenient().when(mapProperties.isEnableServiceMap()).thenReturn(true);
        Duration duration = Duration.ofMinutes(1);
        MapController controller = new MapController(mapProperties, mapService, applicationValidator, serviceModelResolver, commonService, duration);
        lenient().when(applicationValidator.newApplication(any(), any(), anyInt(), any()))
                .thenReturn(new Application("test", ServiceType.STAND_ALONE));

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new ServiceParamArgumentResolver(List.of(new DefaultServiceNameExtractor())))
                .build();
    }

    @Test
    void getServerMapData() throws Exception {
        ResultActions resultActions = mockMvc.perform(get("/api/servermap/serverMap")
                        .param("applicationName", "testApp")
//                        .param("serviceTypeCode", String.valueOf(123))
                        .param("serviceTypeName", ServiceType.STAND_ALONE.getName())
                        .param("from", "0")
                        .param("to", "10000")
                )
                .andDo(print());
//                .andExpect(status().isOk());

        logger.info("resultActions: {}", resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    void getServiceMapData_defaultService_usesSearchOption() throws Exception {
        when(serviceModelResolver.getService(any())).thenReturn(Service.DEFAULT);

        mockMvc.perform(serviceMapRequest()
                        .param("callerRange", "2")
                        .param("calleeRange", "3")
                        .param("bidirectional", "true")
                        .param("wasOnly", "true"))
                .andExpect(status().isOk());

        SearchOption searchOption = captureSearchOption();
        assertEquals(2, searchOption.getOutSearchDepth());
        assertEquals(3, searchOption.getInSearchDepth());
        assertEquals(LinkSelectorType.BIDIRECTIONAL, searchOption.getLinkSelectorType());
        assertTrue(searchOption.isWasOnly());
    }

    @Test
    void getServiceMapData_defaultService_withoutSearchOption_usesFormDefaults() throws Exception {
        when(serviceModelResolver.getService(any())).thenReturn(Service.DEFAULT);

        mockMvc.perform(serviceMapRequest())
                .andExpect(status().isOk());

        SearchOption searchOption = captureSearchOption();
        assertEquals(1, searchOption.getOutSearchDepth());
        assertEquals(1, searchOption.getInSearchDepth());
        assertEquals(LinkSelectorType.BIDIRECTIONAL, searchOption.getLinkSelectorType());
        assertFalse(searchOption.isWasOnly());
    }

    @Test
    void getServiceMapData_otherService_ignoresSearchOption() throws Exception {
        when(serviceModelResolver.getService(any())).thenReturn(new Service("other", 1));
        when(commonService.selectAllApplicationNames(any(Service.class)))
                .thenReturn(List.of(new Application("test", ServiceType.STAND_ALONE)));

        mockMvc.perform(serviceMapRequest()
                        .param("callerRange", "2")
                        .param("calleeRange", "3")
                        .param("bidirectional", "true")
                        .param("wasOnly", "true"))
                .andExpect(status().isOk());

        SearchOption searchOption = captureSearchOption();
        assertEquals(1, searchOption.getOutSearchDepth());
        assertEquals(1, searchOption.getInSearchDepth());
        assertEquals(LinkSelectorType.UNIDIRECTIONAL, searchOption.getLinkSelectorType());
        assertFalse(searchOption.isWasOnly());
    }

    private MockHttpServletRequestBuilder serviceMapRequest() {
        return get("/api/servermap/serviceMap")
                .param("applicationName", "testApp")
                .param("serviceTypeName", ServiceType.STAND_ALONE.getName())
                .param("from", "0")
                .param("to", "10000");
    }

    private SearchOption captureSearchOption() {
        ArgumentCaptor<MapServiceOption> captor = ArgumentCaptor.forClass(MapServiceOption.class);
        verify(mapService).selectApplicationMap(captor.capture());
        return captor.getValue().getSearchOption();
    }
}
