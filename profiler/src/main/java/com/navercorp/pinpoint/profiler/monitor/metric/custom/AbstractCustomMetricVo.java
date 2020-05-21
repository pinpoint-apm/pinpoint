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

package com.navercorp.pinpoint.profiler.monitor.metric.custom;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.monitor.metric.CustomMetricWrapper;

/**
 * @author Taejin Koo
 */
public abstract class AbstractCustomMetricVo implements CustomMetricVo {

    private final int id;
    private final String metricName;

    protected AbstractCustomMetricVo(CustomMetricWrapper customMetricWrapper) {
        this(customMetricWrapper.getId(), customMetricWrapper.getName());
    }

    protected AbstractCustomMetricVo(int id, String metricName) {
        this.id = id;
        this.metricName = Assert.requireNonNull(metricName, "metricName");
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return metricName;
    }

}
