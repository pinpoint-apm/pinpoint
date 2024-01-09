/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.starter.multi.application.type;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleCollectorTypeParserTest {

    @Test
    public void parseCollectorType_ALL() {
        ApplicationArguments args = new DefaultApplicationArguments("--pinpoint.collector.type=ALL");

        SimpleCollectorTypeParser parser = new SimpleCollectorTypeParser();
        CollectorTypeSet collectorTypes = parser.parse(args);

        assertTrue(collectorTypes.hasType(CollectorType.ALL));
        assertTrue(collectorTypes.hasType(CollectorType.BASIC));
    }

    @Test
    public void parseCollectorType_N() {
        ApplicationArguments args = new DefaultApplicationArguments("--pinpoint.collector.type=BASIC,METRIC");

        SimpleCollectorTypeParser parser = new SimpleCollectorTypeParser();
        CollectorTypeSet collectorTypes = parser.parse(args);

        assertTrue(collectorTypes.hasType(CollectorType.BASIC));
        assertTrue(collectorTypes.hasType(CollectorType.METRIC));

        assertFalse(collectorTypes.hasType(CollectorType.ALL));
    }

    @Test
    public void parseCollectorType_N_PARAMETER() {
        ApplicationArguments args = new DefaultApplicationArguments("--pinpoint.collector.type=BASIC", "--pinpoint.collector.type=METRIC");

        SimpleCollectorTypeParser parser = new SimpleCollectorTypeParser();
        CollectorTypeSet collectorTypes = parser.parse(args);

        assertTrue(collectorTypes.hasType(CollectorType.BASIC));
        assertTrue(collectorTypes.hasType(CollectorType.METRIC));

        assertFalse(collectorTypes.hasType(CollectorType.ALL));
    }

    @Test
    public void parseCollectorType_lower() {
        ApplicationArguments args = new DefaultApplicationArguments("--pinpoint.collector.type=all");

        SimpleCollectorTypeParser parser = new SimpleCollectorTypeParser();
        CollectorTypeSet collectorTypes = parser.parse(args);

        assertTrue(collectorTypes.hasType(CollectorType.ALL));
    }

    @Test
    public void parseCollectorType_error() {
        ApplicationArguments args = new DefaultApplicationArguments("--pinpoint.collector.type=error");

        SimpleCollectorTypeParser parser = new SimpleCollectorTypeParser();

        assertThrows(Exception.class,
                () -> parser.parse(args));
    }
}