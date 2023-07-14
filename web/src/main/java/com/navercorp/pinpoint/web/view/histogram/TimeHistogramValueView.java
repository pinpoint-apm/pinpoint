package com.navercorp.pinpoint.web.view.histogram;

import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesValueView;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TimeHistogramValueView implements TimeSeriesValueView {
    private final String fieldName;

    private final List<? extends Number> valueList;

    public TimeHistogramValueView(String fieldName, List<? extends Number> valueList) {
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName");
        this.valueList = Objects.requireNonNull(valueList, "valueList");
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public List<String> getTags() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends Number> getValues() {
        return valueList;
    }
}
