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
import com.navercorp.pinpoint.web.applicationmap.histogram.LoadHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface TimeViewModel {

    static Builder newBuilder(TimeHistogramFormat format) {
        return new Builder(format);
    }

    class Builder {
        private final TimeHistogramFormat format;

        public Builder(TimeHistogramFormat format) {
            this.format = Objects.requireNonNull(format, "format");
        }

        public List<TimeViewModel> build(Application application, List<TimeHistogram> histogramList) {
            if (TimeHistogramFormat.V1 == format) {
                return new ResponseTimeViewModelList(application, histogramList).build();
            }
            return new LoadTimeViewModelList(histogramList).build();
        }
    }

    class ResponseTimeViewModelList implements TimeViewModel {
        private final Application application;
        private final List<TimeHistogram> histogramList;

        public ResponseTimeViewModelList(Application application, List<TimeHistogram> histogramList) {
            this.application = application;
            this.histogramList = histogramList;
        }

        public List<TimeViewModel> build() {
            final List<TimeViewModel> result = new ArrayList<>(9);
            ServiceType serviceType = application.serviceType();
            HistogramSchema schema = serviceType.getHistogramSchema();

            result.add(new ResponseTimeViewModel(schema.getFastSlot().getSlotName(), getColumnValue(histogramList, SlotType.FAST)));
            result.add(new ResponseTimeViewModel(schema.getNormalSlot().getSlotName(), getColumnValue(histogramList, SlotType.NORMAL)));
            result.add(new ResponseTimeViewModel(schema.getSlowSlot().getSlotName(), getColumnValue(histogramList, SlotType.SLOW)));
            result.add(new ResponseTimeViewModel(schema.getVerySlowSlot().getSlotName(), getColumnValue(histogramList, SlotType.VERY_SLOW)));
            result.add(new ResponseTimeViewModel(schema.getErrorSlot().getSlotName(), getColumnValue(histogramList, SlotType.ERROR)));
            result.add(new ResponseTimeViewModel(ResponseTimeStatics.AVG_ELAPSED_TIME, getAvgValue(histogramList)));
            result.add(new ResponseTimeViewModel(ResponseTimeStatics.MAX_ELAPSED_TIME, getColumnValue(histogramList, SlotType.MAX_STAT)));
            result.add(new ResponseTimeViewModel(ResponseTimeStatics.SUM_ELAPSED_TIME, getColumnValue(histogramList, SlotType.SUM_STAT)));
            result.add(new ResponseTimeViewModel(ResponseTimeStatics.TOTAL_COUNT, getTotalCount(histogramList)));

            return result;
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

    class LoadTimeViewModelList implements TimeViewModel {
        private final List<TimeHistogram> histogramList;

        public LoadTimeViewModelList(List<TimeHistogram> histogramList) {
            this.histogramList = Objects.requireNonNull(histogramList, "histogramList");
        }

        public List<TimeViewModel> build() {
            final List<TimeViewModel> loadTimeViewModelList = new ArrayList<>(histogramList.size());
            for (TimeHistogram timeHistogram : histogramList) {
                if (timeHistogram.getTimeStamp() <= 0) {
                    // Ignored unexpected timestamp
                    continue;
                }
                final LoadTimeViewModel loadTimeViewModel = new LoadTimeViewModel(timeHistogram.getTimeStamp(), new LoadHistogram(timeHistogram));
                loadTimeViewModelList.add(loadTimeViewModel);
            }
            return loadTimeViewModelList;
        }
    }
}
