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

import com.navercorp.pinpoint.common.trace.HistogramSchemas;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Woonduk Kang(emeroad)
 */
public class EmptyActiveTraceHistogramTest {
    @Test
    public void getCounter() {

        ActiveTraceHistogram emptyHistogram = new EmptyActiveTraceHistogram(HistogramSchemas.NORMAL_SCHEMA);

        List<Integer> counter = emptyHistogram.getCounter();
        assertThat(counter).hasSize(4)
                .containsExactly(0, 0, 0, 0);

        assertEquals(0, emptyHistogram.getFastCount());
        assertEquals(0, emptyHistogram.getNormalCount());
        assertEquals(0, emptyHistogram.getSlowCount());
        assertEquals(0, emptyHistogram.getVerySlowCount());
    }

}