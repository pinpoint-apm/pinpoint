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

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.DoubleGauge;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntGauge;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongGauge;

/**
 * @author Taejin Koo
 */
public class CustomMetricWrapperFactory {

    CustomMetricWrapperFactory() {
    }

    IntCounterWrapper create(int id, IntCounter intCounter) {
        return new IntCounterWrapper(id, intCounter);
    }

    LongCounterWrapper create(int id, LongCounter longCounter) {
        return new LongCounterWrapper(id, longCounter);
    }

    IntGaugeWrapper create(int id, IntGauge intGauge) {
        return new IntGaugeWrapper(id, intGauge);
    }

    LongGaugeWrapper create(int id, LongGauge longGauge) {
        return new LongGaugeWrapper(id, longGauge);
    }

    DoubleGaugeWrapper create(int id, DoubleGauge doubleGauge) {
        return new DoubleGaugeWrapper(id, doubleGauge);
    }

}
