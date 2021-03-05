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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class ActiveTraceHistogramUtils {

    private static final List<Integer> ZERO_LIST = Arrays.asList(0, 0, 0, 0);

    private ActiveTraceHistogramUtils() {
    }

    public static List<Integer> asList(ActiveTraceHistogram activeTraceHistogram) {
        Objects.requireNonNull(activeTraceHistogram, "activeTraceHistogram");

        if (activeTraceHistogram instanceof EmptyActiveTraceHistogram) {
            return ZERO_LIST;
        }

        return Arrays.asList(activeTraceHistogram.getFastCount(), activeTraceHistogram.getNormalCount(), activeTraceHistogram.getSlowCount(), activeTraceHistogram.getVerySlowCount());

    }
}
