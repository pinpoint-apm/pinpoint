package com.navercorp.pinpoint.common.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

public class YAMLMapper {
    private final ObjectMapper mapper;

    public YAMLMapper(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public ObjectMapper mapper() {
        return mapper;
    }
}
