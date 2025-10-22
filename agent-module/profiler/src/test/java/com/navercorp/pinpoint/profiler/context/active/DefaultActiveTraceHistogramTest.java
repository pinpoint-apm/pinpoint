/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchemas;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultActiveTraceHistogramTest {

    @Test
    void getCounter() {
        HistogramSchema schema = HistogramSchemas.NORMAL_SCHEMA;
        DefaultActiveTraceHistogram histogram = new DefaultActiveTraceHistogram(schema);

        for (int i = 0; i < 1; i++) {
            histogram.increment(schema.getFastSlot());
        }

        for (int i = 0; i < 2; i++) {
            histogram.increment(schema.getNormalSlot());
        }

        for (int i = 0; i < 3; i++) {
            histogram.increment(schema.getSlowSlot());
        }

        for (int i = 0; i < 4; i++) {
            histogram.increment(schema.getVerySlowSlot());
        }

        List<Integer> counter = histogram.getCounter();
        assertThat(counter).hasSize(4)
                .containsExactly(1, 2, 3, 4);
    }
}