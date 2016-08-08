/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author emeroad
 */
public class SpanIdMatcher {
    private List<SpanBo> nextSpanBoList;

    private static final long MAX_EXCLUDE_WEIGHT = 1000 * 5;

    public SpanIdMatcher(List<SpanBo> nextSpanBoList) {
        if (nextSpanBoList == null) {
            throw new NullPointerException("nextSpanBoList must not be null");
        }
        this.nextSpanBoList = nextSpanBoList;
    }

    public SpanBo approximateMatch(long spanEventBoStartTime) {
        // TODO: need algorithm for matching
        List<WeightSpanBo> weightSpanList = computeWeight(spanEventBoStartTime);
        if (weightSpanList.isEmpty()) {
            return null;
        }
        Collections.sort(weightSpanList, new Comparator<WeightSpanBo>() {
            @Override
            public int compare(WeightSpanBo wSpan1, WeightSpanBo wSpan2) {
                final long spanWeight1 = wSpan1.getWeight();
                final long spanWeight2 = wSpan2.getWeight();
                if (spanWeight1 < spanWeight2) {
                    return -1;
                } else {
                    if (spanWeight1 == spanWeight2) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            }
        });

        SpanBo minWeight = getMinWeight(weightSpanList);
        if (minWeight != null) {
            nextSpanBoList.remove(minWeight);
        }
        return minWeight;
    }

    private SpanBo getMinWeight(List<WeightSpanBo> weightSpanList) {
        long min = Long.MAX_VALUE;
        final List<SpanBo> minValue = new ArrayList<>();
        for (WeightSpanBo weightSpanBo : weightSpanList) {
            long weight = weightSpanBo.getWeight();
            if (weight <= min) {
                minValue.add(weightSpanBo.getSpanBo());
                min = weight;
            }
        }

        if (minValue.size() == 1) {
            return minValue.get(0);
        }

        // returns the first data when more than one
        // TODO: we probably need to log this
        return minValue.get(0);
    }

    private List<WeightSpanBo> computeWeight(long spanEventBoStartTime) {
        List<WeightSpanBo> weightSpanList = new ArrayList<>();
        for (SpanBo next : nextSpanBoList) {
            long startTime = next.getStartTime();
            long distance = startTime - spanEventBoStartTime;
            long weightDistance = getWeightDistance(distance);
            if (weightDistance > MAX_EXCLUDE_WEIGHT) {
                // if higher than MAX WEIGHT, most likely missing case. just drop it
                continue;
            }
            weightSpanList.add(new WeightSpanBo(weightDistance, next));
        }
        return weightSpanList;
    }

    private long getWeightDistance(long distance) {
        if (distance >= 0) {
            // positive number
            return distance;
        } else {
            // give a penalty when negative
            // if time skew due to network time sync problem is not big, it is highly unlikely to match a negative number
            // it actually is more likely to get higher positive number diff due to JVM GC.
            // TODO: need to adjust for network sync time diff. penalty is just set to 1 second
            distance = Math.abs(distance);
            return (distance * 2) + 1000;
        }
    }


    public List<SpanBo> other() {
        if (nextSpanBoList.isEmpty()) {
            return null;
        }
        return nextSpanBoList;
    }

    private static class WeightSpanBo {
        private long weight;
        private SpanBo spanBo;

        private WeightSpanBo(long weight, SpanBo spanBo) {
            this.weight = weight;
            this.spanBo = spanBo;
        }

        private long getWeight() {
            return weight;
        }

        private SpanBo getSpanBo() {
            return spanBo;
        }
    }
}
