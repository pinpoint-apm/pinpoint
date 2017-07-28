/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.active;


import com.google.common.primitives.Ints;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class EmptyActiveTraceHistogram implements ActiveTraceHistogram {

    private static final int SLOT_SIZE = DefaultActiveTraceHistogram.SLOT_SIZE;
    private final List<Integer> zeroList;
    private final HistogramSchema histogramSchema;


    public EmptyActiveTraceHistogram(HistogramSchema histogramSchema) {
        this.histogramSchema = Assert.requireNonNull(histogramSchema, "histogramSchema must not be null");

        this.zeroList = Collections.unmodifiableList(Ints.asList(0, 0, 0, 0));
        if (zeroList.size() != SLOT_SIZE) {
            throw new IllegalStateException("zeroList.size() must be " + SLOT_SIZE);
        }
    }

    @Override
    public HistogramSchema getHistogramSchema() {
        return histogramSchema;
    }

    @Override
    public List<Integer> getActiveTraceCounts() {
        return zeroList;
    }


}
