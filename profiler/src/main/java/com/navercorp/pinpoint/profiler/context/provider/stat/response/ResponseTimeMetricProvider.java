/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.response;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.metric.response.DefaultResponseTimeMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class ResponseTimeMetricProvider implements Provider<ResponseTimeMetric> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ResponseTimeCollector responseTimeCollector;

    @Inject
    public ResponseTimeMetricProvider(ResponseTimeCollector responseTimeCollector) {
        this.responseTimeCollector = Assert.requireNonNull(responseTimeCollector, "responseTimeCollector must not be null");
    }

    @Override
    public ResponseTimeMetric get() {
        return new DefaultResponseTimeMetric(responseTimeCollector);
    }

}
