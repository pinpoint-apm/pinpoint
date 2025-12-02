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

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;
import org.eclipse.collections.api.factory.primitive.LongLists;
import org.eclipse.collections.api.list.primitive.LongList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToLongFunction;

public class TimeseriesHistogramViewBuilder {
    private final Application application;
    private final List<TimeHistogram> histogramList;

    public TimeseriesHistogramViewBuilder(Application application, List<TimeHistogram> histogramList) {
        this.application = Objects.requireNonNull(application, "application");
        this.histogramList = Objects.requireNonNull(histogramList, "histogramList");
    }

    public List<TimeHistogramViewModel> build() {
        final List<TimeHistogramViewModel> result = new ArrayList<>(9);
        ServiceType serviceType = application.getServiceType();
        HistogramSchema schema = serviceType.getHistogramSchema();

        result.add(new TimeseriesHistogramView(schema.getFastSlot().getSlotName(), getColumnValue(histogramList, Histogram::getFastCount)));
        result.add(new TimeseriesHistogramView(schema.getNormalSlot().getSlotName(),  getColumnValue(histogramList, Histogram::getNormalCount)));
        result.add(new TimeseriesHistogramView(schema.getSlowSlot().getSlotName(),  getColumnValue(histogramList, Histogram::getSlowCount)));
        result.add(new TimeseriesHistogramView(schema.getVerySlowSlot().getSlotName(),  getColumnValue(histogramList, Histogram::getVerySlowCount)));
        result.add(new TimeseriesHistogramView(schema.getTotalErrorView().getSlotName(),  getColumnValue(histogramList, Histogram::getTotalErrorCount)));

        result.add(new TimeseriesHistogramView(ResponseTimeStatics.AVG_ELAPSED_TIME,  getColumnValue(histogramList, Histogram::getAvgElapsed)));

        result.add(new TimeseriesHistogramView(ResponseTimeStatics.MAX_ELAPSED_TIME,  getColumnValue(histogramList, Histogram::getMaxElapsed)));
        result.add(new TimeseriesHistogramView(ResponseTimeStatics.SUM_ELAPSED_TIME,  getColumnValue(histogramList, Histogram::getSumElapsed)));
        result.add(new TimeseriesHistogramView(ResponseTimeStatics.TOTAL_COUNT,  getColumnValue(histogramList, Histogram::getTotalCount)));

        return result;
    }

    private LongList getColumnValue(List<TimeHistogram> histogramList, ToLongFunction<TimeHistogram> function) {
        final int size = histogramList.size();
        long[] result = new long[size];
        for (int i = 0; i < size; i++) {
            TimeHistogram timeHistogram = histogramList.get(i);
            result[i++] = function.applyAsLong(timeHistogram);
        }
        return LongLists.mutable.of(result);
    }

}
