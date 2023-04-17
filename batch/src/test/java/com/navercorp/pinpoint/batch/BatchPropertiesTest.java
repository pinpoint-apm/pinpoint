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

import com.navercorp.pinpoint.batch.common.BatchProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Woonduk Kang(emeroad)
 */
@TestPropertySource(locations = "classpath:batch-root.properties",
        properties = {"batch.flink.server=1,2"})
@ContextConfiguration(classes = BatchProperties.class)
@ExtendWith(SpringExtension.class)
public class BatchPropertiesTest {

    @Autowired
    BatchProperties properties;

    @Test
    public void test() {
        assertThat(properties)
                .extracting(BatchProperties::getBatchEnv, BatchProperties::getFlinkServerList)
                .containsExactly("release", List.of("1", "2"));
    }

    @Test
    public void cleanupInactiveAgentsConfigurationTest() {
        properties.setup();

        assertThat(properties)
                .extracting(BatchProperties::isEnableCleanupInactiveAgents,
                        BatchProperties::getCleanupInactiveAgentsCron,
                        BatchProperties::getCleanupInactiveAgentsDurationDays)
                .containsExactly(false, "0 0 0 29 2 ?", 30);

    }

}