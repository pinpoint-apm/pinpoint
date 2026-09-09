package com.navercorp.pinpoint.collector.heatmap.service;

import com.navercorp.pinpoint.collector.heatmap.config.HeatmapProperties;
import com.navercorp.pinpoint.collector.heatmap.counter.HeatmapCounter;
import com.navercorp.pinpoint.collector.heatmap.dao.HeatmapDao;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapAgentStat;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStat;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStatKey;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeatmapServiceTest {

    @Mock
    private HeatmapDao heatmapDao;
    @Mock
    private HeatmapProperties heatmapProperties;

    private SpanBo newSpan(String agentId, long acceptTime, int elapsed, int errCode) {
        SpanBo spanBo = new SpanBo();
        spanBo.getSpanOwner().setServiceName("svc");
        spanBo.getSpanOwner().setApplicationName("app");
        spanBo.getSpanOwner().setAgentId(agentId);
        spanBo.setCollectorAcceptTime(acceptTime);
        spanBo.setElapsed(elapsed);
        spanBo.setErrCode(errCode);
        return spanBo;
    }

    @Test
    void spansAreCountedPerKeyNotSentDirectly() {
        when(heatmapProperties.isAppEnabled()).thenReturn(true);
        when(heatmapProperties.isAgentEnabled()).thenReturn(false);
        HeatmapCounter<HeatmapStatKey> counter = new HeatmapCounter<>();
        HeatmapService service = new HeatmapService(heatmapDao, Optional.of(counter), heatmapProperties);

        service.insertSpan(newSpan("agent1", 10_001, 150, 0));
        service.insertSpan(newSpan("agent2", 19_999, 199, 0));
        service.insertSpan(newSpan("agent1", 20_000, 199, 0));
        service.insertSpan(newSpan("agent1", 20_000, 199, 1));
        service.insertSpan(newSpan("agent1", 20_001, 1, 0));

        verify(heatmapDao, never()).insert(any(), anyLong());
        Map<HeatmapStatKey, Long> snapshot = counter.snapshotAndClear();
        assertEquals(4, snapshot.size());
        assertEquals(1L, snapshot.get(new HeatmapStatKey("svc", "app", "agent1", 10_000, 200, true)));
        assertEquals(1L, snapshot.get(new HeatmapStatKey("svc", "app", "agent2", 10_000, 200, true)));
        assertEquals(2L, snapshot.get(new HeatmapStatKey("svc", "app", "agent1", 20_000, 200, true)));
        assertEquals(1L, snapshot.get(new HeatmapStatKey("svc", "app", "agent1", 20_000, 200, false)));
    }

    @Test
    void withoutCounterSendsPerSpan() {
        when(heatmapProperties.isAppEnabled()).thenReturn(true);
        when(heatmapProperties.isAgentEnabled()).thenReturn(false);
        HeatmapService service = new HeatmapService(heatmapDao, Optional.empty(), heatmapProperties);

        service.insertSpan(newSpan("agent1", 10_001, 150, 0));
        service.insertSpan(newSpan("agent1", 10_002, 150, 0));

        verify(heatmapDao, times(2)).insert(any(HeatmapStat.class));
    }

    @Test
    void agentLevelSharesTheCounter() {
        when(heatmapProperties.isAppEnabled()).thenReturn(false);
        when(heatmapProperties.isAgentEnabled()).thenReturn(true);
        HeatmapCounter<HeatmapStatKey> counter = new HeatmapCounter<>();
        HeatmapService service = new HeatmapService(heatmapDao, Optional.of(counter), heatmapProperties);

        service.insertSpan(newSpan("agent1", 10_001, 150, 0));
        service.insertSpan(newSpan("agent1", 10_002, 150, 0));

        verify(heatmapDao, never()).insertAgentStat(any(HeatmapAgentStat.class));
        assertEquals(2L, counter.snapshotAndClear().get(new HeatmapStatKey("svc", "app", "agent1", 10_000, 200, true)));
    }

    @Test
    void withoutCounterSendsAgentStatPerSpan() {
        when(heatmapProperties.isAppEnabled()).thenReturn(false);
        when(heatmapProperties.isAgentEnabled()).thenReturn(true);
        HeatmapService service = new HeatmapService(heatmapDao, Optional.empty(), heatmapProperties);

        service.insertSpan(newSpan("agent1", 10_001, 150, 0));

        verify(heatmapDao, never()).insert(any(HeatmapStat.class));
        verify(heatmapDao, times(1)).insertAgentStat(any(HeatmapAgentStat.class));
    }

    @Test
    void bothDisabledCountsNothing() {
        when(heatmapProperties.isAppEnabled()).thenReturn(false);
        when(heatmapProperties.isAgentEnabled()).thenReturn(false);
        HeatmapCounter<HeatmapStatKey> counter = new HeatmapCounter<>();
        HeatmapService service = new HeatmapService(heatmapDao, Optional.of(counter), heatmapProperties);

        service.insertSpan(newSpan("agent1", 10_001, 150, 0));

        assertTrue(counter.snapshotAndClear().isEmpty());
    }

    @Test
    void negativeElapsedIsIgnored() {
        when(heatmapProperties.isAppEnabled()).thenReturn(true);
        when(heatmapProperties.isAgentEnabled()).thenReturn(false);
        HeatmapCounter<HeatmapStatKey> counter = new HeatmapCounter<>();
        HeatmapService service = new HeatmapService(heatmapDao, Optional.of(counter), heatmapProperties);

        service.insertSpan(newSpan("agent1", 10_001, -1, 0));

        assertTrue(counter.snapshotAndClear().isEmpty());
    }
}
