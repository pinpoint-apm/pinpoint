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

import com.navercorp.pinpoint.collector.heatmap.dao.HeatmapDao;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStat;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author minwoo-jung
 */
@Service
public class HeatmapService implements TraceService {

    private final HeatmapDao heatmapDao;

    public HeatmapService(HeatmapDao heatmapDao) {
        this.heatmapDao = Objects.requireNonNull(heatmapDao, "heatmapDao");
    }

    @Override
    public void insertSpanChunk(SpanChunkBo spanChunkBo) {
    }

    @Override
    public void insertSpan(SpanBo spanBo) {
        HeatmapStat heatmapStat = new HeatmapStat(spanBo.getApplicationId(), spanBo.getAgentId(), spanBo.getCollectorAcceptTime(), spanBo.getElapsed(), spanBo.getErrCode());
        heatmapDao.insert(heatmapStat);
    }
}
