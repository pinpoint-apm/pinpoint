package com.navercorp.pinpoint.metric.common.model.json;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class Tags {
    private final List<Tag> tags = new ArrayList<>();

    @JsonAnySetter
    public void add(String name, String value) {
        tags.add(new Tag(name, value));
    }

    public List<Tag> getTags() {
        return tags;
    }
}
