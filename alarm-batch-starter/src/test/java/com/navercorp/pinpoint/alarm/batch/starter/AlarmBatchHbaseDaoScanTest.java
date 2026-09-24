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

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.ApplicationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.AgentIdRowKeyEncoder;
import com.navercorp.pinpoint.common.server.dao.AgentIdDao;
import com.navercorp.pinpoint.common.server.dao.ApplicationDao;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the dao package scan leaves behind, without a cluster to reach.
 *
 * <p>A dao picked up by the scan is wired from whatever the context happens to hold, which is
 * the one thing the compiler cannot check: a dao asking for a bean this process does not define
 * starts nothing, and a dao also declared by hand leaves two of the same type for an injection
 * point that wants one. Neither shows up until the process is started against hbase.
 */
class AlarmBatchHbaseDaoScanTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(StubCluster.class, ScannedDaos.class);

    @Test
    void theScanLeavesExactlyOneOfEachDaoTheAlarmReads() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBeansOfType(AgentIdDao.class)).hasSize(1);
            assertThat(context.getBeansOfType(ApplicationDao.class)).hasSize(1);
        });
    }

    @Test
    void theDeployableDoesNotAlsoDeclareOneTheScanProvides() {
        runner.run(context -> {
            Set<Class<?>> scanned = List.of(AgentIdDao.class, ApplicationDao.class).stream()
                    .filter(type -> !context.getBeansOfType(type).isEmpty())
                    .collect(Collectors.toSet());
            assertThat(scanned).isNotEmpty();

            for (Method method : AlarmBatchHbaseConfiguration.class.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Bean.class)) {
                    continue;
                }
                assertThat(scanned)
                        .as("%s declares a dao the scan already provides, leaving two of its type"
                                + " for an injection point that wants one", method.getName())
                        .noneMatch(type -> type.isAssignableFrom(method.getReturnType()));
            }
        });
    }

    /** The package the deployable scans, on its own. */
    @Configuration(proxyBeanMethods = false)
    @ComponentScan("com.navercorp.pinpoint.common.server.dao.hbase")
    static class ScannedDaos {
    }

    /** What a dao in that package may ask this process for. */
    @Configuration(proxyBeanMethods = false)
    static class StubCluster {
        @Bean
        HbaseOperations hbaseTemplate() {
            return Mockito.mock(HbaseOperations.class);
        }

        @Bean
        TableNameProvider tableNameProvider() {
            return Mockito.mock(TableNameProvider.class);
        }

        @Bean
        ApplicationFactory applicationFactory() {
            return Mockito.mock(ApplicationFactory.class);
        }

        @Bean
        AgentIdRowKeyEncoder agentIdRowKeyEncoder() {
            return Mockito.mock(AgentIdRowKeyEncoder.class);
        }
    }
}
