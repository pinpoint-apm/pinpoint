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
import com.navercorp.pinpoint.common.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class ActiveTraceHistogramUtils {

    private static final List<Integer> ZERO_LIST = Collections.unmodifiableList(Ints.asList(0, 0, 0, 0));

    private ActiveTraceHistogramUtils() {
    }

    public static List<Integer> asList(ActiveTraceHistogram activeTraceHistogram) {
        Assert.requireNonNull(activeTraceHistogram, "activeTraceHistogram must not be null");

        if (activeTraceHistogram instanceof EmptyActiveTraceHistogram) {
            return ZERO_LIST;
        }

        return Ints.asList(activeTraceHistogram.getFastCount(), activeTraceHistogram.getNormalCount(), activeTraceHistogram.getSlowCount(), activeTraceHistogram.getVerySlowCount());

    }
}
