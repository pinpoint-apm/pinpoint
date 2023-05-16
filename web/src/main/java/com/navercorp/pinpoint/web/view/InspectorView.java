package com.navercorp.pinpoint.web.view;

import com.navercorp.pinpoint.web.metric.common.model.Tag;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorValue;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorValueGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorData;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InspectorView {
    private final InspectorData inspectorData;

    public InspectorView(InspectorData inspectorData) {
        this.inspectorData = Objects.requireNonNull(inspectorData, "systemMetricData");
    }

    public String getTitle() {
        return inspectorData.getTitle();
    }

    public String getUnit() {
        return inspectorData.getUnit();
    }

    public List<Long> getTimestamp() {
        return inspectorData.getTimeStampList();
    }

    public List<InspectorValueGroupView> getMetricValueGroups() {
        return inspectorData.getMetricValueGroupList()
                .stream()
                .map(InspectorValueGroupView::new)
                .collect(Collectors.toList());
    }

    public static class InspectorValueGroupView {
        private final InspectorValueGroup valueGroup;

        public InspectorValueGroupView(InspectorValueGroup valueGroup) {
            this.valueGroup = Objects.requireNonNull(valueGroup, "valueGroup");
        }

        public String getGroupName() {
            return valueGroup.getGroupName();
        }

        public List<InspectorValueView> getMetricValues() {
            return valueGroup.getMetricValueList()
                    .stream()
                    .map(InspectorValueView::new)
                    .collect(Collectors.toList());
        }
    }

    public static class InspectorValueView {
        private final InspectorValue<?> value;

        public InspectorValueView(InspectorValue<?> value) {
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