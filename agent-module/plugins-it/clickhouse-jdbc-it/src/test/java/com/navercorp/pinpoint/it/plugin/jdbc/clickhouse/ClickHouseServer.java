/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.jdbc.clickhouse;

import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;

/**
 * @author intr3p1d
 */
public class ClickHouseServer implements SharedTestLifeCycle {
    private static final String CLICKHOUSE_IMAGE = "clickhouse/clickhouse-server:23.9.1.1854";
    private ClickHouseContainer container;

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");
        container = new ClickHouseContainer(DockerImageName.parse(CLICKHOUSE_IMAGE));
        container.withDatabaseName("default");
        container.withUsername("default");
        container.withPassword("");
        container.withInitScript("sql/init_clickhouse.sql");
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
