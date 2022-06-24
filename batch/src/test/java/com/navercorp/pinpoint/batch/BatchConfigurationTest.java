/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.batch;

import com.navercorp.pinpoint.batch.common.BatchConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
@TestPropertySource(locations = "classpath:batch-root.properties",
        properties = {"batch.flink.server=1,2"})
@ContextConfiguration(classes = BatchConfiguration.class)
@ExtendWith(SpringExtension.class)
public class BatchConfigurationTest {

    @Autowired
    BatchConfiguration configuration;

    @Test
    public void test() {
        Assertions.assertEquals("release", configuration.getBatchEnv());
        Assertions.assertEquals(Arrays.asList("1", "2"), configuration.getFlinkServerList());
    }

    @Test
    public void cleanupInactiveAgentsConfigurationTest() {
        configuration.setup();

        boolean enableCleanupInactiveAgents = configuration.isEnableCleanupInactiveAgents();
        String cleanupInactiveAgentsCron = configuration.getCleanupInactiveAgentsCron();
        int cleanupInactiveAgentsDurationDays = configuration.getCleanupInactiveAgentsDurationDays();

        Assertions.assertEquals(false, enableCleanupInactiveAgents);
        Assertions.assertEquals("0 0 0 29 2 ?", cleanupInactiveAgentsCron);
        Assertions.assertEquals(30, cleanupInactiveAgentsDurationDays);
    }

}