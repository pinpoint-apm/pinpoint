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

package com.navercorp.pinpoint.web.applicationmap.view;

import org.eclipse.collections.api.list.primitive.LongList;

import java.util.Objects;

/**
 * @author emeroad
 */
public class TimeseriesHistogramView implements TimeHistogramViewModel {
    private final String key;
    private final LongList values;

    public TimeseriesHistogramView(String key, LongList values) {
        this.key = Objects.requireNonNull(key, "key");
        this.values = Objects.requireNonNull(values, "values");
    }

    public String getKey() {
        return key;
    }


    public LongList getValues() {
        return values;
    }
}
