/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.web.view;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.ArrayList;
import java.util.List;

public interface TimeViewModel {

    class TimeViewModelBuilder {
        private final Application application;
        private final List<TimeHistogram> histogramList;

        public TimeViewModelBuilder(Application application, List<TimeHistogram> histogramList) {
            this.application = application;
            this.histogramList = histogramList;
        }

        public List<TimeViewModel> build() {
            return createViewModel();
        }

        List<TimeViewModel> createViewModel() {
            final List<TimeViewModel> value = new ArrayList<>(9);
            ServiceType serviceType = application.getServiceType();
            HistogramSchema schema = serviceType.getHistogramSchema();
            value.add(new ResponseTimeViewModel(schema.getFastSlot().getSlotName(), getColumnValue(histogramList, SlotType.FAST)));
            value.add(new ResponseTimeViewModel(schema.getNormalSlot().getSlotName(), getColumnValue(histogramList, SlotType.NORMAL)));
            value.add(new ResponseTimeViewModel(schema.getSlowSlot().getSlotName(), getColumnValue(histogramList, SlotType.SLOW)));
            value.add(new ResponseTimeViewModel(schema.getVerySlowSlot().getSlotName(), getColumnValue(histogramList, SlotType.VERY_SLOW)));
            value.add(new ResponseTimeViewModel(schema.getErrorSlot().getSlotName(), getColumnValue(histogramList, SlotType.ERROR)));
            value.add(new ResponseTimeViewModel(ResponseTimeStatics.AVG_ELAPSED_TIME, getAvgValue(histogramList)));
            value.add(new ResponseTimeViewModel(ResponseTimeStatics.MAX_ELAPSED_TIME, getColumnValue(histogramList, SlotType.MAX_STAT)));
            value.add(new ResponseTimeViewModel(ResponseTimeStatics.SUM_ELAPSED_TIME, getColumnValue(histogramList, SlotType.SUM_STAT)));
            value.add(new ResponseTimeViewModel(ResponseTimeStatics.TOTAL_COUNT, getTotalCount(histogramList)));

            return value;
        }

        List<ResponseTimeViewModel.TimeCount> getColumnValue(List<TimeHistogram> histogramList, SlotType slotType) {
            List<ResponseTimeViewModel.TimeCount> result = new ArrayList<>(histogramList.size());
            for (TimeHistogram timeHistogram : histogramList) {
                final long timeStamp = timeHistogram.getTimeStamp();
                ResponseTimeViewModel.TimeCount TimeCount = new ResponseTimeViewModel.TimeCount(timeStamp, getCount(timeHistogram, slotType));
                result.add(TimeCount);
            }
            return result;
        }

        List<ResponseTimeViewModel.TimeCount> getAvgValue(List<TimeHistogram> histogramList) {
            List<ResponseTimeViewModel.TimeCount> result = new ArrayList<>(histogramList.size());
            for (TimeHistogram timeHistogram : histogramList) {
                final long timeStamp = timeHistogram.getTimeStamp();
                final long totalCount = timeHistogram.getTotalCount();
                final long sumElapsed = getCount(timeHistogram, SlotType.SUM_STAT);
                final long avgElapsed = totalCount > 0 ? sumElapsed / totalCount : 0L;

                result.add(new ResponseTimeViewModel.TimeCount(timeStamp, avgElapsed));
            }
            return result;
        }

        List<ResponseTimeViewModel.TimeCount> getTotalCount(List<TimeHistogram> histogramList) {
            List<ResponseTimeViewModel.TimeCount> result = new ArrayList<>(histogramList.size());
            for (TimeHistogram timeHistogram : histogramList) {
                final long timeStamp = timeHistogram.getTimeStamp();
                final long totalCount = timeHistogram.getTotalCount();
                result.add(new ResponseTimeViewModel.TimeCount(timeStamp, totalCount));
            }
            return result;
        }

        long getCount(TimeHistogram timeHistogram, SlotType slotType) {
            return timeHistogram.getCount(slotType);
        }
    }
}
