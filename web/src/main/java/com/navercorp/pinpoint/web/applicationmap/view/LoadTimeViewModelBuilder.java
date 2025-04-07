package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.web.applicationmap.histogram.LoadHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoadTimeViewModelBuilder {
    private final List<TimeHistogram> histogramList;

    public LoadTimeViewModelBuilder(List<TimeHistogram> histogramList) {
        this.histogramList = Objects.requireNonNull(histogramList, "histogramList");
    }

    public List<TimeHistogramViewModel> build() {
        final List<TimeHistogramViewModel> loadTimeViewModelList = new ArrayList<>(histogramList.size());
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