package com.navercorp.pinpoint.metric.collector;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectorTypeParserTest {

    @Test
    public void parseCollectorType_ALL() {
        ApplicationArguments args = new DefaultApplicationArguments("--pinpoint.collector.type=ALL");

        CollectorTypeParser parser = new CollectorTypeParser();
        TypeSet collectorTypes = parser.parse(args);

        assertTrue(collectorTypes.hasType(CollectorType.ALL));
        assertTrue(collectorTypes.hasType(CollectorType.BASIC));
    }

    @Test
    public void parseCollectorType_N() {
        ApplicationArguments args = new DefaultApplicationArguments("--pinpoint.collector.type=BASIC,METRIC");

        CollectorTypeParser parser = new CollectorTypeParser();
        TypeSet collectorTypes = parser.parse(args);

        assertTrue(collectorTypes.hasType(CollectorType.BASIC));
        assertTrue(collectorTypes.hasType(CollectorType.METRIC));

        assertFalse(collectorTypes.hasType(CollectorType.ALL));
    }

    @Test
    public void parseCollectorType_N_PARAMETER() {
        ApplicationArguments args = new DefaultApplicationArguments("--pinpoint.collector.type=BASIC", "--pinpoint.collector.type=METRIC");

        CollectorTypeParser parser = new CollectorTypeParser();
        TypeSet collectorTypes = parser.parse(args);

        assertTrue(collectorTypes.hasType(CollectorType.BASIC));
        assertTrue(collectorTypes.hasType(CollectorType.METRIC));

        assertFalse(collectorTypes.hasType(CollectorType.ALL));
    }

    @Test
    public void parseCollectorType_lower() {
        ApplicationArguments args = new DefaultApplicationArguments("--pinpoint.collector.type=all");

        CollectorTypeParser parser = new CollectorTypeParser();
        TypeSet collectorTypes = parser.parse(args);

        assertTrue(collectorTypes.hasType(CollectorType.ALL));
    }

    @Test
    public void parseCollectorType_error() {
        ApplicationArguments args = new DefaultApplicationArguments("--pinpoint.collector.type=error");

        CollectorTypeParser parser = new CollectorTypeParser();

        assertThrows(Exception.class,
                () -> parser.parse(args));
    }
}