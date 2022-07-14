package com.navercorp.pinpoint.web.view.timeseries;

import com.navercorp.pinpoint.web.metric.common.model.Tag;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TimeSeriesView {
    private final TimeSeriesData timeSeriesData;

    public TimeSeriesView(TimeSeriesData timeSeriesData) {
        this.timeSeriesData = Objects.requireNonNull(timeSeriesData, "systemMetricData");
    }

    public String getTitle() {
        return timeSeriesData.getTitle();
    }

    public String getUnit() {
        return timeSeriesData.getUnit();
    }

    public List<Long> getTimestamp() {
        return timeSeriesData.getTimeStampList();
    }

    public List<TimeSeriesValueGroupView> getMetricValueGroups() {
        return timeSeriesData.getMetricValueGroupList()
                .stream()
                .map(TimeSeriesValueGroupView::new)
                .collect(Collectors.toList());
    }

    public static class TimeSeriesValueGroupView {
        private final TimeSeriesValueGroup valueGroup;

        public TimeSeriesValueGroupView(TimeSeriesValueGroup valueGroup) {
            this.valueGroup = Objects.requireNonNull(valueGroup, "valueGroup");
        }

        public String getGroupName() {
            return valueGroup.getGroupName();
        }

        public List<TimeSeriesValueView> getMetricValues() {
            return valueGroup.getMetricValueList()
                    .stream()
                    .map(TimeSeriesValueView::new)
                    .collect(Collectors.toList());
        }
    }

    public static class TimeSeriesValueView {
        private final TimeSeriesValue value;

        public TimeSeriesValueView(TimeSeriesValue value) {
            this.value = Objects.requireNonNull(value, "value");
        }

        public String getFieldName() {
            return value.getFieldName();
        }

        public List<String> getTags() {
            return value.getTagList()
                    .stream()
                    .map(Tag::toString)
                    .collect(Collectors.toList());
        }

        public List<?> getValues() {
            return value.getValueList();
        }
    }
}