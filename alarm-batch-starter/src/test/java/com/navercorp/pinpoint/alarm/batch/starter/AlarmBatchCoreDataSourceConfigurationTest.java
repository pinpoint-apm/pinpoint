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
package com.navercorp.pinpoint.alarm.batch.starter;

import com.navercorp.pinpoint.alarm.core.agentstat.dao.AgentStatAlarmDao;
import com.navercorp.pinpoint.alarm.core.vo.CoreAlarmDataSource;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.service.AlarmDataSourceProvider;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.common.server.bo.ApplicationFactory;
import com.navercorp.pinpoint.common.server.dao.AgentEventDao;
import com.navercorp.pinpoint.common.server.dao.AgentIdDao;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * That every data source this deployable offers has something to read it.
 *
 * <p>A data source the operator can pick with no service registered for it is the worst of the
 * failure modes here: such a rule is never read at all, so nothing is evaluated, nothing is
 * reported, and the rule looks configured to whoever set it up. The reverse -- two services
 * claiming one data source -- makes which one answers depend on bean ordering. Neither shows
 * up in a compile.
 */
class AlarmBatchCoreDataSourceConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            // The resolver memoises for the length of a sweep, so it is handed the sweep's
            // own delay. Supplied here rather than defaulted in the configuration: a value
            // this process cannot run without should not be guessable.
            .withPropertyValues("pinpoint.modules.batch.alarm.fixedDelayMs=10000")
            .withUserConfiguration(StubStores.class, AlarmBatchCoreDataSourceConfiguration.class);

    @Test
    void everyOfferedDataSourceHasExactlyOneQueryService() {
        runner.run(context -> {
            List<AlarmDataSource> offered = context.getBean(AlarmDataSourceProvider.class)
                    .dataSources();
            List<MetricQueryService> services =
                    List.copyOf(context.getBeansOfType(MetricQueryService.class).values());

            Set<AlarmDataSource> claimed = services.stream()
                    .map(MetricQueryService::getDataSource)
                    .collect(Collectors.toSet());

            assertEquals(Set.copyOf(offered), claimed,
                    "every data source offered to an operator needs a service, and only one");
            assertEquals(services.size(), claimed.size(),
                    "two services claiming one data source makes the answer depend on bean order");
        });
    }

    @Test
    void theOfferedDataSourcesAreTheCoreOnes() {
        runner.run(context -> assertEquals(
                List.of(CoreAlarmDataSource.values()),
                context.getBean(AlarmDataSourceProvider.class).dataSources()));
    }

    @Configuration(proxyBeanMethods = false)
    static class StubStores {

        @Bean
        MapResponseDao mapResponseDao() {
            return Mockito.mock(MapResponseDao.class);
        }

        @Bean
        MapOutLinkDao mapOutLinkDao() {
            return Mockito.mock(MapOutLinkDao.class);
        }

        @Bean
        AgentEventDao agentEventDao() {
            return Mockito.mock(AgentEventDao.class);
        }

        @Bean
        AgentStatAlarmDao agentStatAlarmDao() {
            return Mockito.mock(AgentStatAlarmDao.class);
        }

        @Bean
        AgentIdDao agentIdDao() {
            return Mockito.mock(AgentIdDao.class);
        }

        @Bean
        ApplicationFactory applicationFactory() {
            return Mockito.mock(ApplicationFactory.class);
        }

        @Bean
        ServiceModelResolver serviceModelResolver() {
            return Mockito.mock(ServiceModelResolver.class);
        }
    }
}
