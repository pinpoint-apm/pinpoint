package com.navercorp.pinpoint.metric.web.view;

import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.model.MetricValue;
import com.navercorp.pinpoint.metric.web.model.MetricValueGroup;
import com.navercorp.pinpoint.metric.web.model.SystemMetricData;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SystemMetricView {
    private final SystemMetricData<? extends Number> systemMetricData;

    public SystemMetricView(SystemMetricData<? extends Number> systemMetricData) {
        this.systemMetricData = Objects.requireNonNull(systemMetricData, "systemMetricData");
    }

    public String getTitle() {
        return systemMetricData.getTitle();
    }

    public String getUnit() {
        return systemMetricData.getUnit();
    }

    public List<Long> getTimestamp() {
        return systemMetricData.getTimeStampList();
    }

    public List<MetricValueGroupView> getMetricValueGroups() {
        return systemMetricData.getMetricValueGroupList()
                .stream()
                .map(MetricValueGroupView::new)
                .collect(Collectors.toList());
    }

    public static class MetricValueGroupView {
        private final MetricValueGroup<? extends Number> value;

        public MetricValueGroupView(MetricValueGroup value) {
            this.value = Objects.requireNonNull(value, "value");
        }

        public String getGroupName() {
            return value.getGroupName();
        }

        public List<MetricValueView> getMetricValues() {
            return value.getMetricValueList()
                    .stream()
                    .map(MetricValueView::new)
                    .collect(Collectors.toList());
        }
    }

    public static class MetricValueView {
        private final MetricValue<? extends Number> value;

        public MetricValueView(MetricValue<? extends Number> value) {
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

        public List<? extends Number> getValues() {
            return value.getValueList();
        }
    }
}
