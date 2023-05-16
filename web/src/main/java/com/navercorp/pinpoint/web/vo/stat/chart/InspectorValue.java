package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.metric.common.model.Tag;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

public class InspectorValue<Y> {
    private final String fieldName;
    private final List<Tag> tagList;
    private final List<Y> valueList;

    public InspectorValue(String fieldName, List<Tag> tagList, List<Y> valueList) {
        Assert.hasLength(fieldName, "fieldName must not be empty");
        this.fieldName = fieldName;
        this.tagList = Objects.requireNonNull(tagList, "tagList");
        this.valueList = Objects.requireNonNull(valueList, "valueList");
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public List<Y> getValueList() {
        return valueList;
    }

    public String getFieldName() {
        return fieldName;
    }
}