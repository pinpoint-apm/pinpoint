/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.web.controller;

import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.alarm.web.service.AlarmChannelService;
import com.navercorp.pinpoint.alarm.web.service.AlarmRuleService;
import com.navercorp.pinpoint.alarm.web.view.AlarmNotificationChannelApiMapper;
import com.navercorp.pinpoint.service.web.vo.ServiceConstants;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlarmControllerHeaderTest {

    @Test
    void createRuleUsesDefaultServiceNameWhenHeaderIsMissing() throws Exception {
        AlarmRuleService service = ruleService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmRuleController(service)).build();

        mockMvc.perform(post("/api/alarm/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ruleBody()))
                .andExpect(status().isCreated());

        assertEquals(ServiceConstants.DEFAULT, createdRule(service).getServiceName());
    }

    @Test
    void createRuleUsesServiceNameHeader() throws Exception {
        AlarmRuleService service = ruleService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmRuleController(service)).build();

        mockMvc.perform(post("/api/alarm/rule")
                        .header(ServiceConstants.KEY, "service-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ruleBody()))
                .andExpect(status().isCreated());

        assertEquals("service-a", createdRule(service).getServiceName());
    }

    @Test
    void createChannelUsesDefaultServiceNameWhenHeaderIsMissing() throws Exception {
        AlarmChannelService service = channelService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmChannelController(
                service, new AlarmNotificationChannelApiMapper(new ObjectMapper()))).build();

        mockMvc.perform(post("/api/alarm/channel")
                        .param("applicationName", "test-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(channelBody()))
                .andExpect(status().isCreated());

        assertEquals(ServiceConstants.DEFAULT, createdChannel(service).getServiceName());
        assertEquals("{\"format\":\"SLACK\"}", createdChannel(service).getConfig());
    }

    @Test
    void createChannelUsesServiceNameHeader() throws Exception {
        AlarmChannelService service = channelService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmChannelController(
                service, new AlarmNotificationChannelApiMapper(new ObjectMapper()))).build();

        mockMvc.perform(post("/api/alarm/channel")
                        .header(ServiceConstants.KEY, "service-a")
                        .param("applicationName", "test-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(channelBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.config.format").value("SLACK"));

        assertEquals("service-a", createdChannel(service).getServiceName());
    }

    @Test
    void listRulesTakesTheServiceFromTheHeader() throws Exception {
        AlarmRuleService service = ruleService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmRuleController(service)).build();

        // The header is the only place the writes read the service from, so the reads
        // have to agree with them.
        mockMvc.perform(get("/api/alarm/rule")
                        .header(ServiceConstants.KEY, "service-a")
                        .param("applicationName", "test-app")
                        .param("applicationType", "javascript"))
                .andExpect(status().isOk());

        assertEquals("service-a", listedApplication(service).getServiceName());
    }

    private static String ruleBody() {
        return """
                {
                  "name": "rule",
                  "severity": "CRITICAL",
                  "dataSource": "AGENT_STAT",
                  "applicationName": "test-app",
                  "applicationType": "javascript",
                  "checkIntervalSec": 60,
                  "actionIntervalSec": 60,
                  "conditions": {
                    "type": "LEAF",
                    "metric": "sample_count",
                    "op": ">=",
                    "threshold": 100.0,
                    "windowSec": 300,
                    "aggregation": "COUNT"
                  }
                }
                """;
    }

    private static String channelBody() {
        return """
                {
                  "channelName": "webhook",
                  "methodType": "WEBHOOK",
                  "destination": "https://example.com/hook",
                  "config": {
                    "format": "SLACK"
                  }
                }
                """;
    }

    private static AlarmRuleService ruleService() {
        AlarmRuleService service = mock(AlarmRuleService.class);
        when(service.getEffectiveRulesByApplication(any())).thenReturn(List.of());
        when(service.createRule(any())).thenAnswer(invocation -> {
            AlarmRuleV2 rule = invocation.getArgument(0);
            rule.setId(1L);
            rule.setDataSource(TestAlarmDataSource.AGENT_STAT.name());
            return rule;
        });
        return service;
    }

    private static AlarmChannelService channelService() {
        AlarmChannelService service = mock(AlarmChannelService.class);
        when(service.createChannel(any())).thenAnswer(invocation -> {
            AlarmNotificationChannel channel = invocation.getArgument(0);
            channel.setId(1L);
            return channel;
        });
        return service;
    }

    private static AlarmRuleV2 createdRule(AlarmRuleService service) {
        ArgumentCaptor<AlarmRuleV2> captor = ArgumentCaptor.forClass(AlarmRuleV2.class);
        verify(service).createRule(captor.capture());
        return captor.getValue();
    }

    private static AlarmNotificationChannel createdChannel(AlarmChannelService service) {
        ArgumentCaptor<AlarmNotificationChannel> captor = ArgumentCaptor.forClass(AlarmNotificationChannel.class);
        verify(service).createChannel(captor.capture());
        return captor.getValue();
    }

    private static AlarmApplication listedApplication(AlarmRuleService service) {
        ArgumentCaptor<AlarmApplication> captor = ArgumentCaptor.forClass(AlarmApplication.class);
        verify(service).getEffectiveRulesByApplication(captor.capture());
        return captor.getValue();
    }
}
