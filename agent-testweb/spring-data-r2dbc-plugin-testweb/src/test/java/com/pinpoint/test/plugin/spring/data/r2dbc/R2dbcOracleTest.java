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

package com.pinpoint.test.plugin.spring.data.r2dbc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public class R2dbcOracleTest {
    public static final String ORACLE_18_X_IMAGE = "gvenzl/oracle-xe:18-slim";
    public static final String ORACLE_21_X_IMAGE = "gvenzl/oracle-xe:21-slim";
    public static final String DATABASE_NAME = "test";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";

    private static OracleContainer container;

    @BeforeAll
    public static void beforeClass() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");
        Assumptions.assumeFalse(DockerTestUtils.isArmDockerServer());

        container = new OracleContainer(ORACLE_21_X_IMAGE);
        container.setWaitStrategy(Wait.forLogMessage(".*Completed.*", 1));
        container.withStartupTimeout(Duration.ofSeconds(300));
        container.addEnv("DBCA_ADDITIONAL_PARAMS", "-initParams sga_target=0M pga_aggreegate_target=0M");
        container.withReuse(true);
        container.withInitScript("oracle-init.sql");

        container.start();

        System.out.println("##databaseName=" + container.getDatabaseName());
        System.out.println("##host=" + container.getHost());
        System.out.println("##port=" + container.getFirstMappedPort());
        System.out.println("##user=" + container.getUsername());
        System.out.println("##password=" + container.getPassword());
    }

    @AfterAll
    public static void select() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    public void test() throws Exception {
        System.out.println("TEST");
    }

}
