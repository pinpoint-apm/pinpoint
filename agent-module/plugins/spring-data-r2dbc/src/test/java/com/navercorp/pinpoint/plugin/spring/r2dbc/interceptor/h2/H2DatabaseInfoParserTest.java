/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.h2;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class H2DatabaseInfoParserTest {

    @Test
    void getHostListAndDatabase() {

        assertDatabaseInfoEquals("jdbc:h2:mem:test_mem", "local", "mem:test_mem");

        assertDatabaseInfoEquals("jdbc:h2:tcp://dbserv:8084/~/sample", "dbserv:8084", "~/sample");

        assertDatabaseInfoEquals("jdbc:h2:~/test", "local", "~/test");

        assertDatabaseInfoEquals("jdbc:h2:file:/data/sample", "local", "/data/sample");

        assertDatabaseInfoEquals("jdbc:h2:file:C:/data/sample", "local", "C:/data/sample");

        assertDatabaseInfoEquals("jdbc:h2:mem:", "local", "unnamed-in-memory");

        assertDatabaseInfoEquals("jdbc:h2:tcp://localhost/~/test", "localhost", "~/test");

        assertDatabaseInfoEquals("jdbc:h2:tcp://localhost/mem:test", "localhost", "mem:test");

        assertDatabaseInfoEquals("jdbc:h2:ssl://localhost:8085/~/sample;", "localhost:8085", "~/sample");
    }

    private static void assertDatabaseInfoEquals(String url, String host, String database) {
        H2DatabaseInfoParser parser = new H2DatabaseInfoParser(url);
        assertThat(parser.getHostList()).containsExactly(host);
        assertThat(parser.getDatabase()).isEqualTo(database);
    }
}