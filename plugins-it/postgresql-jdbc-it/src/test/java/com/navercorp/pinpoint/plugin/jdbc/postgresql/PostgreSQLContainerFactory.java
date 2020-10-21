/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.postgresql;

import org.slf4j.Logger;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PostgreSQLContainerFactory {
    public static JdbcDatabaseContainer newContainer(Logger logger) {
        PostgreSQLContainer container = new PostgreSQLContainer();
        container.withInitScript("init_postgresql.sql");
        container.withLogConsumer(new Slf4jLogConsumer(logger));
        return container;
    }
}
