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

package com.navercorp.pinpoint.web.applicationmap.view;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
public class TimeseriesHistogramViewModel implements TimeHistogramViewModel {
    private final String key;
    private final List<? extends Number> values;

    public TimeseriesHistogramViewModel(String key, List<? extends Number> values) {
        this.key = Objects.requireNonNull(key, "key");
        this.values = Objects.requireNonNull(values, "values");
    }

    public String getKey() {
        return key;
    }


    public List<? extends Number> getValues() {
        return values;
    }
}
