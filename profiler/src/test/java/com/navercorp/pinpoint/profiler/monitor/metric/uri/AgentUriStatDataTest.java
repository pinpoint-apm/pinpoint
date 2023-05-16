package com.navercorp.pinpoint.profiler.monitor.metric.uri;

import com.navercorp.pinpoint.common.profiler.clock.Clock;
import com.navercorp.pinpoint.common.profiler.clock.TickClock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AgentUriStatDataTest {
    private static final int DEFAULT_COLLECT_INTERVAL = 30000; // 30s

    @Test
    public void add_capacity() {
        TickClock clock = (TickClock) Clock.tick(DEFAULT_COLLECT_INTERVAL);

        long baseTimestamp = System.currentTimeMillis();
        AgentUriStatData metric = new AgentUriStatData(baseTimestamp, 1, clock);

        UriStatInfo test = new UriStatInfo("test", true, 10, baseTimestamp);

        Assertions.assertTrue(metric.add(test));
        Assertions.assertFalse(metric.add(test));
    }

    @Test
    public void add_pattern() {
        TickClock clock = (TickClock) Clock.tick(DEFAULT_COLLECT_INTERVAL);

        long baseTimestamp = System.currentTimeMillis();
        AgentUriStatData metric = new AgentUriStatData(baseTimestamp, 10, clock);

        UriStatInfo pattern1 = new UriStatInfo("pattern1", true, 10, baseTimestamp);
        metric.add(pattern1);
        UriStatInfo pattern1_1 = new UriStatInfo("pattern1", true, 10, baseTimestamp);
        metric.add(pattern1_1);

        UriStatInfo pattern2 = new UriStatInfo("pattern2", true, 10, baseTimestamp);
        metric.add(pattern2);

        assertThat(metric.getAllUriStatData()).hasSize(2);

        Optional<EachUriStatData> findPattern1 = metric.getAllUriStatData()
                .stream()
                .map(Map.Entry::getValue)
                .filter(eachUriStatData -> eachUriStatData.getUri().equals("pattern1"))
                .findFirst();
        Assertions.assertEquals(2, findPattern1.get().getTotalHistogram().getCount());

    }
}