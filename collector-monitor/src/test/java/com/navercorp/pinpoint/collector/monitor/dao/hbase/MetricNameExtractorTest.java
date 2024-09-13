package com.navercorp.pinpoint.collector.monitor.dao.hbase;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author intr3p1d
 */
class MetricNameExtractorTest {

    static final String METRIC_NAME = "org.apache.hadoop.hbase.client.MetricsConnection.executorPoolActiveThreads";
    static final String DUMMY_CLUSTER_ID = "f72a0b6a-8141-4df9-96a3-754aac08e173";
    static final String DUMMY_HASH = "10579683cf";

    @Test
    public void testCustomName() {
        String example = METRIC_NAME + "." + DUMMY_CLUSTER_ID + "@" + DUMMY_HASH;
        String actual = MetricNameExtractor.extractName(example);

        Assertions.assertEquals(METRIC_NAME, actual);
    }

    @Test
    public void testExtractTags() {
        String example = METRIC_NAME + "." + DUMMY_CLUSTER_ID + "@" + DUMMY_HASH;
        Tags expected = Tags.of(
                Tag.of("clusterId", DUMMY_CLUSTER_ID),
                Tag.of("connectionHash", DUMMY_HASH)
        );
        Tags actual = MetricNameExtractor.extractTags(example);

        Assertions.assertEquals(expected, actual);
    }

}