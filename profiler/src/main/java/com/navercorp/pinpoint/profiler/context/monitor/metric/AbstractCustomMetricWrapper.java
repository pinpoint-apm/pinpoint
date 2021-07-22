/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.monitor.metric;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetric;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
abstract class AbstractCustomMetricWrapper<T extends CustomMetric> implements CustomMetricWrapper {

    private final int id;
    protected final T customMetric;

    public AbstractCustomMetricWrapper(int id, T customMetric) {
        this.id = id;
        this.customMetric = Objects.requireNonNull(customMetric, "customMetric");
    }

    @Override
    public String getName() {
        return customMetric.getName();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equalsWithUnwrap(Object object) {
        if (object == null) {
            return false;
        }
        return customMetric == object;
    }

}
