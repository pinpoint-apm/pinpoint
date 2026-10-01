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

package com.navercorp.pinpoint.collector.heatmap.dao;

import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapAgentStat;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStat;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStatKey;

import java.util.concurrent.CompletableFuture;

/**
 * @author minwoo-jung
 */
public interface HeatmapDao {
    CompletableFuture<?> insert(HeatmapStat heatmapStat);

    CompletableFuture<?> insert(HeatmapStatKey key, long count);

    CompletableFuture<?> insertAgentStat(HeatmapAgentStat heatmapAgentStat);

    CompletableFuture<?> insertAgentStat(HeatmapStatKey key, long count);
}
