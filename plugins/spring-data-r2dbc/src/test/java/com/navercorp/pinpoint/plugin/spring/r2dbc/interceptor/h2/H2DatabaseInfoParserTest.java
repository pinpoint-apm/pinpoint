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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class H2DatabaseInfoParserTest {

    @Test
    void getHostListAndDatabase() {
        H2DatabaseInfoParser parser = new H2DatabaseInfoParser("jdbc:h2:mem:test_mem");
        List<String> list = parser.getHostList();
        assertEquals(1, list.size());
        assertEquals("local", parser.getHostList().get(0));
        assertEquals("mem:test_mem", parser.getDatabase());

        parser = new H2DatabaseInfoParser("jdbc:h2:tcp://dbserv:8084/~/sample");
        assertEquals("dbserv:8084", parser.getHostList().get(0));
        assertEquals("~/sample", parser.getDatabase());

        parser = new H2DatabaseInfoParser("jdbc:h2:~/test");
        assertEquals("local", parser.getHostList().get(0));
        assertEquals("~/test", parser.getDatabase());

        parser = new H2DatabaseInfoParser("jdbc:h2:file:/data/sample");
        assertEquals("local", parser.getHostList().get(0));
        assertEquals("/data/sample", parser.getDatabase());

        parser = new H2DatabaseInfoParser("jdbc:h2:file:C:/data/sample");
        assertEquals("local", parser.getHostList().get(0));
        assertEquals("C:/data/sample", parser.getDatabase());

        parser = new H2DatabaseInfoParser("jdbc:h2:mem:");
        assertEquals("local", parser.getHostList().get(0));
        assertEquals("unnamed-in-memory", parser.getDatabase());

        parser = new H2DatabaseInfoParser("jdbc:h2:tcp://localhost/~/test");
        assertEquals("localhost", parser.getHostList().get(0));
        assertEquals("~/test", parser.getDatabase());

        parser = new H2DatabaseInfoParser("jdbc:h2:tcp://localhost/mem:test");
        assertEquals("localhost", parser.getHostList().get(0));
        assertEquals("mem:test", parser.getDatabase());

        parser = new H2DatabaseInfoParser("jdbc:h2:ssl://localhost:8085/~/sample;");
        assertEquals("localhost:8085", parser.getHostList().get(0));
        assertEquals("~/sample", parser.getDatabase());
    }
}