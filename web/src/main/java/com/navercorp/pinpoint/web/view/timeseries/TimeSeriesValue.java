package com.navercorp.pinpoint.web.view.timeseries;

import com.navercorp.pinpoint.web.metric.common.model.Tag;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

public class TimeSeriesValue {
    private final String fieldName;
    private final List<Tag> tagList;
    private final List<?> valueList;

    public TimeSeriesValue(String fieldName, List<Tag> tagList, List<?> valueList) {
        Assert.hasLength(fieldName, "fieldName must not be empty");
        this.fieldName = fieldName;
        this.tagList = Objects.requireNonNull(tagList, "tagList");
        this.valueList = Objects.requireNonNull(valueList, "valueList");
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public List<?> getValueList() {
        return valueList;
    }

    public String getFieldName() {
        return fieldName;
    }
}