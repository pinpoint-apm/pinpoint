/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.inspector;

import java.util.List;
import java.util.Objects;

/**
 * The parts of an OTLP explicit-bucket histogram point this module uses. {@code sum} is NaN when the
 * point does not carry the optional {@code sum} field (the mean and the total are then unknown). {@code max} is the
 * optional {@code max} field; {@code hasMax} is false when the SDK did not send it. {@code bounds}
 * has N entries and {@code bucketCounts} N+1 (the last one is the overflow bucket), or both are empty.
 */
public record HistogramValue(long count, double sum, boolean hasMax, double max,
                             List<Double> bounds, List<Long> bucketCounts) {

    public HistogramValue {
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(bucketCounts, "bucketCounts");
    }

    public static HistogramValue of(long count, double sum) {
        return new HistogramValue(count, sum, false, 0, List.of(), List.of());
    }

    /**
     * Upper bound of the highest non-empty bucket: the best estimate of the maximum when the
     * {@code max} field is absent or (cumulative input) covers the whole process lifetime. Falls back
     * to the mean when there are no buckets. When only the overflow bucket is populated its upper
     * bound is unknown, so the estimate is the last finite bound or the mean, whichever is larger
     * (never below the average). Returns -1 when the histogram is empty.
     */
    public double approximateMax() {
        if (count <= 0) {
            return -1;
        }
        if (bucketCounts.isEmpty()) {
            return sum / count;
        }
        for (int i = bucketCounts.size() - 1; i >= 0; i--) {
            if (bucketCounts.get(i) > 0) {
                if (i < bounds.size()) {
                    return bounds.get(i);
                }
                // overflow bucket: only its lower bound is known
                if (bounds.isEmpty()) {
                    return sum / count;
                }
                return Math.max(bounds.get(bounds.size() - 1), sum / count);
            }
        }
        return sum / count;
    }
}
