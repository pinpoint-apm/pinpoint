/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.heatmap.service;

import com.navercorp.pinpoint.collector.heatmap.config.HeatmapProperties;
import com.navercorp.pinpoint.collector.heatmap.counter.HeatmapCounter;
import com.navercorp.pinpoint.collector.heatmap.dao.HeatmapDao;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapAgentStat;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStat;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStatKey;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.timeseries.window.DefaultTimeSlot;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author minwoo-jung
 */
@Service
public class HeatmapService implements TraceService {

    // matches roundedEventTime granularity of the pinot heatmap tables
    private static final long TIME_SLOT_RESOLUTION = 10_000L;

    private final Logger logger = LogManager.getLogger(getClass());
    private final ThrottledLogger tLogger = ThrottledLogger.getUncountedIntervalLogger(logger);

    private final HeatmapDao heatmapDao;
    // null when aggregation is disabled, spans are sent one by one
    private final HeatmapCounter<HeatmapStatKey> statCounter;
    private final TimeSlot timeSlot = new DefaultTimeSlot(TIME_SLOT_RESOLUTION);
    private final boolean appEnabled;
    private final boolean agentEnabled;

    public HeatmapService(HeatmapDao heatmapDao, Optional<HeatmapCounter<HeatmapStatKey>> statCounter, HeatmapProperties heatmapProperties) {
        this.heatmapDao = Objects.requireNonNull(heatmapDao, "heatmapDao");
        this.statCounter = statCounter.orElse(null);
        Objects.requireNonNull(heatmapProperties, "heatmapProperties");
        this.appEnabled = heatmapProperties.isAppEnabled();
        this.agentEnabled = heatmapProperties.isAgentEnabled();
        logger.info("HeatmapService aggregation:{}", this.statCounter != null);
    }

    @Override
    public void insertSpanChunk(SpanChunkBo spanChunkBo) {
    }

    @Override
    public void insertSpan(SpanBo spanBo) {
        if (spanBo.getElapsed() < 0) {
            tLogger.warn("elapsedTime is negative. agentId={}, elapsed={}", spanBo.getAgentId(), spanBo.getElapsed());
            return;
        }
        if (!appEnabled && !agentEnabled) {
            return;
        }
        // one counter feeds both topics, the flusher fans out per enabled level
        if (statCounter != null) {
            HeatmapStatKey key = HeatmapStatKey.of(spanBo, timeSlot);
            statCounter.increment(key);
            return;
        }
        if (appEnabled) {
            HeatmapStat heatmapStat = new HeatmapStat(spanBo.getServiceName(), spanBo.getApplicationName(), spanBo.getAgentId(), spanBo.getCollectorAcceptTime(), spanBo.getElapsed(), spanBo.getErrCode());
            heatmapDao.insert(heatmapStat);
        }
        if (agentEnabled) {
            HeatmapAgentStat heatmapAgentStat = new HeatmapAgentStat(spanBo.getServiceName(), spanBo.getApplicationName(), spanBo.getAgentId(), spanBo.getCollectorAcceptTime(), spanBo.getElapsed(), spanBo.getErrCode());
            heatmapDao.insertAgentStat(heatmapAgentStat);
        }
    }
}
