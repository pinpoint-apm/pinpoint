/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.collector.starter.multi.application.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RunCommandCollectorTypeParserTest {

    private final RunCommandCollectorTypeParser parser = new RunCommandCollectorTypeParser();

    @Test
    public void noArgs_defaultsToAll() {
        CollectorTypeSet types = parser.parse(new String[0]);
        assertTrue(types.hasType(CollectorType.ALL));
    }

    @Test
    public void run_defaultsToAll() {
        CollectorTypeSet types = parser.parse(new String[]{"run"});
        assertTrue(types.hasType(CollectorType.ALL));
    }

    @Test
    public void run_typesOption() {
        CollectorTypeSet types = parser.parse(new String[]{"run", "--types", "basic,log"});
        assertTrue(types.hasType(CollectorType.BASIC));
        assertTrue(types.hasType(CollectorType.LOG));
        assertFalse(types.hasType(CollectorType.METRIC));
    }

    @Test
    public void run_typesOptionWithEquals() {
        CollectorTypeSet types = parser.parse(new String[]{"run", "--types=METRIC"});
        assertTrue(types.hasType(CollectorType.METRIC));
        assertFalse(types.hasType(CollectorType.BASIC));
    }

    @Test
    public void run_positionalTypes() {
        CollectorTypeSet types = parser.parse(new String[]{"run", "OTLPTRACE"});
        assertTrue(types.hasType(CollectorType.OTLPTRACE));
    }

    @Test
    public void unknownCommand_empty() {
        CollectorTypeSet types = parser.parse(new String[]{"--spring.profiles.active=release"});
        assertTrue(types.isEmpty());
    }

    @Test
    public void invalidType_empty() {
        CollectorTypeSet types = parser.parse(new String[]{"run", "--types", "NOPE"});
        assertTrue(types.isEmpty());
    }
}
