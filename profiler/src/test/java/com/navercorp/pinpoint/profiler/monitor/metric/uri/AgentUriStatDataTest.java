package com.navercorp.pinpoint.profiler.monitor.metric.uri;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentUriStatDataTest {

    @Test
    public void add_capacity() {
        long baseTimestamp = System.currentTimeMillis();
        AgentUriStatData metric = new AgentUriStatData(baseTimestamp, 1);

        UriStatInfo test = new UriStatInfo("test", true, 10, baseTimestamp);

        Assertions.assertTrue(metric.add(test));
        Assertions.assertFalse(metric.add(test));
    }

    @Test
    public void add_pattern() {
        long baseTimestamp = System.currentTimeMillis();
        AgentUriStatData metric = new AgentUriStatData(baseTimestamp, 10);

        UriStatInfo pattern1 = new UriStatInfo("pattern1", true, 10, baseTimestamp);
        metric.add(pattern1);
        UriStatInfo pattern1_1 = new UriStatInfo("pattern1", true, 10, baseTimestamp);
        metric.add(pattern1_1);

        UriStatInfo pattern2 = new UriStatInfo("pattern2", true, 10, baseTimestamp);
        metric.add(pattern2);

        Assertions.assertEquals(2, metric.getAllUriStatData().size());

        Optional<EachUriStatData> findPattern1 = metric.getAllUriStatData()
                .stream()
                .map(Map.Entry::getValue)
                .filter(eachUriStatData -> eachUriStatData.getUri().equals("pattern1"))
                .findFirst();
        Assertions.assertEquals(2, findPattern1.get().getTotalHistogram().getCount());

    }
}