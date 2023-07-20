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
import org.testcontainers.containers.MySQLContainer;

public class R2dbcMysqlTest {
    public static final String DATABASE_NAME = "test";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "";

    private static MySQLContainer container = new MySQLContainer();

    @BeforeAll
    public static void beforeClass() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        container = new MySQLContainer("mysql:5.7.34");
        container.withDatabaseName(DATABASE_NAME);
        container.withUsername(USERNAME);
        container.withPassword(PASSWORD);
        container.withInitScript("mysql-init.sql");
        container.withUrlParam("serverTimezone", "UTC");
        container.withUrlParam("useSSL", "false");
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
