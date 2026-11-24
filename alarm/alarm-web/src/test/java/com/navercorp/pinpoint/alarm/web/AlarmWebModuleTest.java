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

import com.navercorp.pinpoint.alarm.web.controller.AlarmCatalogController;
import com.navercorp.pinpoint.alarm.web.controller.AlarmChannelController;
import com.navercorp.pinpoint.alarm.web.controller.AlarmRuleController;
import com.navercorp.pinpoint.alarm.web.controller.AlarmTemplateController;
import tools.jackson.databind.json.JsonMapper;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceProvider;
import com.navercorp.pinpoint.alarm.vo.TestAlarmDataSource;
import com.navercorp.pinpoint.common.server.dao.ApplicationDao;
import com.navercorp.pinpoint.mybatis.DefaultMyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.mybatis.MyBatisConfigurationCustomizer;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import com.navercorp.pinpoint.user.config.UserConfigProperties;
import com.navercorp.pinpoint.user.dao.UserGroupDao;
import com.navercorp.pinpoint.user.service.UserGroupServiceImpl;
import com.navercorp.pinpoint.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class AlarmWebModuleTest {

    /**
     * What a web supplies: the main datasource, its mybatis customizer, the application index,
     * and the user services. The user group service is the real one, because it takes the alarm
     * deletion listener and so closes a bean cycle through the channel service.
     */
    @Configuration
    @Import(UserGroupServiceImpl.class)
    static class WebBeans {
        @Bean
        DataSource dataSource() {
            return mock(DataSource.class);
        }

        @Bean
        MyBatisConfigurationCustomizer myBatisConfigurationCustomizer() {
            return new DefaultMyBatisConfigurationCustomizer();
        }

        @Bean
        JsonMapper jsonMapper() {
            return new JsonMapper();
        }

        @Bean
        ApplicationDao applicationDao() {
            return mock(ApplicationDao.class);
        }

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        UserGroupDao mysqlUserGroupDao() {
            return mock(UserGroupDao.class);
        }

        @Bean
        UserConfigProperties userConfigProperties() {
            return new UserConfigProperties();
        }

        @Bean
        ServiceModelResolver serviceModelResolver() {
            return mock(ServiceModelResolver.class);
        }

        @Bean
        AlarmDataSourceProvider dataSourceProvider() {
            return new TestAlarmDataSource.Provider();
        }
    }

    @Test
    void startsWithWhatAWebSupplies() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(WebBeans.class, AlarmWebModule.class)) {
            assertNotNull(context.getBean(AlarmRuleController.class));
            assertNotNull(context.getBean(AlarmChannelController.class));
            assertNotNull(context.getBean(AlarmTemplateController.class));
            assertNotNull(context.getBean(AlarmCatalogController.class));
        }
    }
}
