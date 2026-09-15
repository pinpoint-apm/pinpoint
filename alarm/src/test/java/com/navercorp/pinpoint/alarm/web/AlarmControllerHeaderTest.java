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
package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.service.AlarmApplicationExistenceChecker;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceRegistry;
import com.navercorp.pinpoint.alarm.validation.ConditionValidator;
import com.navercorp.pinpoint.alarm.validation.FilterKeyValidator;
import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.alarm.dao.AlarmChannelBindingDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleLocalConfigDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateItemDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmStateDao;
import com.navercorp.pinpoint.alarm.sender.AlarmNotificationChannelConfigParser;
import com.navercorp.pinpoint.alarm.service.EffectiveAlarmRuleResolver;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.service.web.vo.ServiceConstants;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlarmControllerHeaderTest {

    @Test
    void createRuleUsesDefaultServiceNameWhenHeaderIsMissing() throws Exception {
        RecordingAlarmRuleService service = new RecordingAlarmRuleService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmRuleController(service)).build();

        mockMvc.perform(post("/api/alarm/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ruleBody()))
                .andExpect(status().isCreated());

        assertEquals(ServiceConstants.DEFAULT, service.createdRuleServiceName);
    }

    @Test
    void createRuleUsesServiceNameHeader() throws Exception {
        RecordingAlarmRuleService service = new RecordingAlarmRuleService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmRuleController(service)).build();

        mockMvc.perform(post("/api/alarm/rule")
                        .header(ServiceConstants.KEY, "service-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ruleBody()))
                .andExpect(status().isCreated());

        assertEquals("service-a", service.createdRuleServiceName);
    }

    @Test
    void createChannelUsesDefaultServiceNameWhenHeaderIsMissing() throws Exception {
        RecordingAlarmChannelService service = new RecordingAlarmChannelService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmChannelController(
                service, new AlarmNotificationChannelApiMapper(new ObjectMapper()))).build();

        mockMvc.perform(post("/api/alarm/channel")
                        .param("applicationName", "test-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(channelBody()))
                .andExpect(status().isCreated());

        assertEquals(ServiceConstants.DEFAULT, service.createdChannelServiceName);
        assertEquals("{\"format\":\"SLACK\"}", service.createdChannelConfig);
    }

    @Test
    void createChannelUsesServiceNameHeader() throws Exception {
        RecordingAlarmChannelService service = new RecordingAlarmChannelService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmChannelController(
                service, new AlarmNotificationChannelApiMapper(new ObjectMapper()))).build();

        mockMvc.perform(post("/api/alarm/channel")
                        .header(ServiceConstants.KEY, "service-a")
                        .param("applicationName", "test-app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(channelBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.config.format").value("SLACK"));

        assertEquals("service-a", service.createdChannelServiceName);
    }

    @Test
    void listRulesTakesTheServiceFromTheHeader() throws Exception {
        RecordingAlarmRuleService service = new RecordingAlarmRuleService();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AlarmRuleController(service)).build();

        // The header is the only place the writes read the service from, so the reads
        // have to agree with them.
        mockMvc.perform(get("/api/alarm/rule")
                        .header(ServiceConstants.KEY, "service-a")
                        .param("applicationName", "test-app")
                        .param("applicationType", "javascript"))
                .andExpect(status().isOk());

        assertEquals("service-a", service.listedApplication.getServiceName());
    }

    private static String ruleBody() {
        return """
                {
                  "name": "rule",
                  "severity": "CRITICAL",
                  "dataSource": "PRIMARY",
                  "applicationName": "test-app",
                  "applicationType": "javascript",
                  "checkIntervalSec": 60,
                  "actionIntervalSec": 60,
                  "conditions": {
                    "type": "LEAF",
                    "metric": "event_count",
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

    private static class RecordingAlarmRuleService extends AlarmRuleService {
        private String createdRuleServiceName;
        private AlarmApplication listedApplication;

        RecordingAlarmRuleService() {
            super(stub(AlarmRuleV2Dao.class),
                    stub(AlarmRuleLocalConfigDao.class),
                    stub(AlarmTemplateDao.class),
                    stub(AlarmTemplateItemDao.class),
                    stub(AlarmChannelBindingDao.class),
                    stub(AlarmHistoryV2Dao.class),
                    stub(AlarmStateDao.class),
                    new EffectiveAlarmRuleResolver(),
                    new AlarmApplicationResolver(
                            List.of(alwaysMissingChecker())),
                    new AlarmBundleLocks(stub(AlarmRuleV2Dao.class),
                            stub(AlarmTemplateDao.class),
                            stub(AlarmTemplateItemDao.class)),
                    new AlarmConfigValidator(stub(AlarmTemplateItemDao.class),
                            new ConditionValidator(),
                            new FilterKeyValidator(),
                            new AlarmDataSourceRegistry(List.of(new TestAlarmDataSource.Provider()))),
                    new AlarmRuleStamper(stub(AlarmRuleV2Dao.class), stub(AlarmStateDao.class)),
                    new AlarmRuleDeleter(stub(AlarmRuleV2Dao.class),
                            stub(AlarmRuleLocalConfigDao.class),
                            stub(AlarmChannelBindingDao.class),
                            stub(AlarmHistoryV2Dao.class),
                            stub(AlarmNotificationOutboxDao.class),
                            stub(AlarmStateDao.class)));
        }

        @Override
        public List<AlarmRuleResponse> getRuleResponsesByApplication(AlarmApplication application) {
            listedApplication = application;
            return List.of();
        }

        @Override
        public AlarmRuleV2 createRule(AlarmRuleV2 rule) {
            createdRuleServiceName = rule.getServiceName();
            rule.setId(1L);
            rule.setDataSource(TestAlarmDataSource.PRIMARY.name());
            return rule;
        }
    }

    private static class RecordingAlarmChannelService extends AlarmChannelService {
        private String createdChannelServiceName;
        private String createdChannelConfig;

        RecordingAlarmChannelService() {
            super(stub(AlarmNotificationChannelDao.class),
                    stub(AlarmChannelBindingDao.class),
                    stub(AlarmRuleV2Dao.class),
                    new AlarmNotificationChannelConfigParser(new ObjectMapper()),
                    new AlarmBundleLocks(stub(AlarmRuleV2Dao.class),
                            stub(AlarmTemplateDao.class),
                            stub(AlarmTemplateItemDao.class)));
        }

        @Override
        public AlarmNotificationChannel createChannel(AlarmNotificationChannel channel) {
            createdChannelServiceName = channel.getServiceName();
            createdChannelConfig = channel.getConfig();
            channel.setId(1L);
            return channel;
        }
    }

    @SuppressWarnings("unchecked")
    /** Finds nothing: these tests never reach the application. */
    private static AlarmApplicationExistenceChecker alwaysMissingChecker() {
        return new AlarmApplicationExistenceChecker() {
            @Override
            public Set<String> supportedTypes() {
                return Set.of(AlarmApplication.TYPE_JAVASCRIPT);
            }

            @Override
            public boolean exists(AlarmApplication application) {
                return false;
            }
        };
    }

    private static <T> T stub(Class<T> type) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> {
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
