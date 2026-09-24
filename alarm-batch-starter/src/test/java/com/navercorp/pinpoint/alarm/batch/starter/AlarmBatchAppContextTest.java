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

import com.navercorp.pinpoint.alarm.service.AlarmNotificationDispatcher;
import org.junit.jupiter.api.Test;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmMetricDefinition;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * That the deployable's wiring resolves.
 *
 * <p>The failure this catches does not show up in a compile: a configuration that declares a
 * bean the shared module also declares, or one that expects a bean nobody supplies, only
 * fails when the context is built. Both happened while this module's dependencies were being
 * moved, and neither was visible to the rest of the suite.
 *
 * <p>It boots the job half rather than the whole process, because the other half opens a
 * connection to hbase while it builds its beans and would make this a test of whether a
 * cluster is reachable. That half is covered by
 * {@link AlarmBatchCoreDataSourceConfigurationTest}, which stands it up against stubs.
 */
@SpringBootTest(classes = {AlarmBatchJobConfiguration.class,
        AlarmBatchAppContextTest.OneDataSource.class})
@ActiveProfiles("local")
@TestPropertySource(properties = {
        "pinpoint.profiles.active=local",
        "spring.batch.jdbc.initialize-schema=always",
        "spring.datasource.hikari.jdbc-url=jdbc:h2:mem:alarm;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.hikari.username=sa",
        "spring.datasource.hikari.password=",
        "spring.datasource.hikari.driver-class-name=org.h2.Driver",
        "spring.meta-datasource.hikari.jdbc-url=jdbc:h2:mem:alarmmeta;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.meta-datasource.hikari.username=sa",
        "spring.meta-datasource.hikari.password=",
        "spring.meta-datasource.hikari.driver-class-name=org.h2.Driver"
})
class AlarmBatchAppContextTest {

    @Autowired
    private Job alarmEvaluationJob;

    @Autowired
    private Tasklet alarmTasklet;

    @Autowired
    private AlarmNotificationDispatcher notificationDispatcher;

    @Autowired
    private Environment environment;

    // The banner splits this on commas and looks each key up, so a key that arrives with
    // surrounding space -- which is what yaml line folding does to a plain scalar -- resolves
    // to nothing and prints as null. An empty list does the same with one empty key.
    @Test
    void everyBannerKeyIsLookedUpAsWritten() {
        String configs = environment.getProperty("pinpoint.banner.configs");
        assertNotNull(configs, "the banner prints one null line when this is unset");

        for (String key : configs.split(",")) {
            assertEquals(key.trim(), key, "this key resolves to nothing: '" + key + "'");
            assertFalse(key.isEmpty(), "an empty key prints as null: " + configs);
        }
    }

    // Boot unions the exclusions of every @EnableAutoConfiguration it finds, so putting one
    // back on the job half would switch these off in whatever imported it -- and nothing
    // about this context would fail to say so.
    @Test
    void theJobHalfDeclaresNoAutoConfigurationOfItsOwn() {
        assertNull(AnnotationUtils.findAnnotation(
                        AlarmBatchJobConfiguration.class, EnableAutoConfiguration.class),
                "the application class declares the exclusions; an imported half must not");
    }

    @Test
    void theJobAndTheOutboxDispatcherAreBothWired() {
        assertNotNull(alarmEvaluationJob);
        assertNotNull(alarmTasklet);
        assertNotNull(notificationDispatcher);
    }

    /**
     * The sweep asks the database only for the data sources it can evaluate, so a process with
     * none would ask for nothing and is rejected at startup. The real process gets its own from
     * {@link AlarmBatchCoreDataSourceConfiguration}; this one only needs the job to be buildable.
     */
    @TestConfiguration(proxyBeanMethods = false)
    // Booting half the application makes this class the entry point, so the exclusions the
    // real one declares are declared here too. They are not on AlarmBatchJobConfiguration,
    // because Boot would then apply them to anything that imported it.
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            SecurityAutoConfiguration.class,
            BatchAutoConfiguration.class
    })
    static class OneDataSource {

        @Bean
        MetricQueryService stubMetricQueryService() {
            return new MetricQueryService() {
                @Override
                public AlarmDataSource getDataSource() {
                    return new AlarmDataSource() {
                        @Override
                        public String name() {
                            return "STUB";
                        }

                        @Override
                        public String label() {
                            return "Stub";
                        }

                        @Override
                        public List<String> filterKeys() {
                            return List.of();
                        }

                        @Override
                        public List<AlarmMetricDefinition> metrics() {
                            return List.of();
                        }
                    };
                }

                @Override
                public MetricQueryResult query(AlarmRuleV2 rule, List<AlarmCondition> conditions,
                                               List<AlarmFilter> filters, AlarmState state) {
                    return MetricQueryResult.empty();
                }
            };
        }
    }
}
