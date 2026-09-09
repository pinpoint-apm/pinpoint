package com.navercorp.pinpoint.collector.heatmap.config;

import com.navercorp.pinpoint.collector.heatmap.dao.HeatmapDao;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStatKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HeatmapSchedulerConfigurationTest {

    @Mock
    private HeatmapDao heatmapDao;

    private final HeatmapStatKey key = new HeatmapStatKey("svc", "app", "agent", 10_000, 200, true);

    @Test
    void fanOutSendsToEachEnabledLevel() {
        HeatmapSchedulerConfiguration.fanOut(heatmapDao, true, true).accept(key, 3L);
        verify(heatmapDao).insert(key, 3L);
        verify(heatmapDao).insertAgentStat(key, 3L);
    }

    @Test
    void fanOutSkipsDisabledLevel() {
        HeatmapSchedulerConfiguration.fanOut(heatmapDao, false, true).accept(key, 3L);
        verify(heatmapDao, never()).insert(key, 3L);
        verify(heatmapDao).insertAgentStat(key, 3L);
    }
}
