/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.metric.activethread;

import java.util.Objects;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;


/**
 * @author HyunGil Jeong
 */
public class DefaultActiveTraceMetric implements ActiveTraceMetric {

    private final ActiveTraceRepository activeTraceRepository;

    public DefaultActiveTraceMetric(ActiveTraceRepository activeTraceRepository) {
        this.activeTraceRepository = Objects.requireNonNull(activeTraceRepository, "activeTraceRepository");
    }

    @Override
    public ActiveTraceHistogram activeTraceHistogram() {
        final long currentTimeMillis = System.currentTimeMillis();
        final ActiveTraceHistogram histogram = activeTraceRepository.getActiveTraceHistogram(currentTimeMillis);

        return histogram;
    }

    @Override
    public String toString() {
        return "DefaultActiveTraceMetric";
    }
}
