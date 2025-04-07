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

package com.navercorp.pinpoint.web.heatmap.vo;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo-jung
 */
public record ElapsedTimeBucketInfo(int min, int max, int timeInterval, List<Integer> bucketList) {

    public ElapsedTimeBucketInfo(List<Integer> bucketList, int timeInterval) {
        this(bucketList.get(0), bucketList.get(bucketList.size() - 1), timeInterval, Objects.requireNonNull(bucketList, "bucketList"));
    }

    public int findLargestMultipleBelow() {
        return bucketList.get(bucketList.size() - 2);
    }

    @Override
    public String toString() {
        return "ElapsedTimeBucketInfo{" +
                "min=" + min +
                ", max=" + max +
                ", timeInterval=" + timeInterval +
                ", bucketList=" + bucketList +
                '}';
    }
}
