/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.it.plugin.utils;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class TestcontainersOption {
    private TestcontainersOption() {
    }

    public static final String VERSION = "1.18.3";

    public static final String TEST_CONTAINER = "org.testcontainers:testcontainers:" + VERSION;
    public static final String MSSQL = "org.testcontainers:mssqlserver:" + VERSION;
    public static final String MYSQLDB = "org.testcontainers:mysql:" + VERSION;
    public static final String MARIADB = "org.testcontainers:mariadb:" + VERSION;
    public static final String POSTGRESQL = "org.testcontainers:postgresql:" + VERSION;
    public static final String ORACLE = "org.testcontainers:oracle-xe:" + VERSION;

    public static final String ELASTICSEARCH = "org.testcontainers:elasticsearch:" + VERSION;
    public static final String MONGODB = "org.testcontainers:mongodb:" + VERSION;
    public static final String KAFKA = "org.testcontainers:kafka:" + VERSION;
    public static final String RABBITMQ = "org.testcontainers:rabbitmq:" + VERSION;
}
