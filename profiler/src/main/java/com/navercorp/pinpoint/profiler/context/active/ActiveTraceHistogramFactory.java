/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.active;

import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

/**
 * @author Taejin Koo
 * @author HyunGil Jeong
 */
public class ActiveTraceHistogramFactory {

    private final ActiveTraceRepository activeTraceRepository;
    private final HistogramSchema histogramSchema = BaseHistogramSchema.NORMAL_SCHEMA;

    private final ActiveTraceHistogram emptyActiveTraceHistogram = new EmptyActiveTraceHistogram(histogramSchema);


    public ActiveTraceHistogramFactory(ActiveTraceRepository activeTraceRepository) {
        this.activeTraceRepository = Assert.requireNonNull(activeTraceRepository, "activeTraceRepository must not be null");
    }

    public ActiveTraceHistogram createHistogram() {

        final List<ActiveTraceSnapshot> collectedActiveTraceInfo = activeTraceRepository.collect();
        if (collectedActiveTraceInfo.isEmpty()) {
            return emptyActiveTraceHistogram;
        }

        final long currentTime = System.currentTimeMillis();
        final DefaultActiveTraceHistogram histogram = new DefaultActiveTraceHistogram(histogramSchema);
        for (ActiveTraceSnapshot activeTraceInfo : collectedActiveTraceInfo) {
            final int elapsedTime = (int) (currentTime - activeTraceInfo.getStartTime());
            final HistogramSlot slot = histogramSchema.findHistogramSlot(elapsedTime, false);
            histogram.increment(slot);
        }

        return histogram;
    }


}
