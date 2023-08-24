/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jdbc.r2dbc;

import com.navercorp.pinpoint.pluginit.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.pluginit.utils.DockerTestUtils;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Properties;

public class MariadbServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    private MariaDBContainer container;

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");
        Assumptions.assumeFalse(DockerTestUtils.isArmDockerServer());

        container = new MariaDBContainer("mariadb:10.3.6");
        container.waitingFor(Wait.forListeningPort());
        container.withInitScript("mariadb-init.sql");
        container.start();

        return DatabaseContainers.toProperties(container);
    }

    @Override
    public void afterAll() {
        if (container != null) {
            container.stop();
        }
    }
}
