package com.navercorp.pinpoint.collector.manage.controller;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.common.server.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleResponse implements Response {
    private final boolean result;
    private final String message;
    private Map<String, Object> attributeMap;

    public static SimpleResponse success() {
        return new SimpleResponse(true);
    }

    public SimpleResponse(boolean result, String message) {
        this.result = result;
        this.message = message;
    }

    public SimpleResponse(boolean result) {
        this.result = result;
        this.message = null;
    }

    public String getResult() {
        if (result) {
            return "success";
        }
        return "fail";
    }

    public String getMessage() {
        return message;
    }

    public void addAttribute(String attributeName, Object attributeValue) {
        Objects.requireNonNull(attributeName, "attributeName");

        Map<String, Object> map = getAttributeMap0();
        map.put(attributeName, attributeValue);
    }


    private Map<String, Object> getAttributeMap0() {
        if (this.attributeMap == null) {
            this.attributeMap = new HashMap<>();
        }
        return this.attributeMap;
    }

    @JsonAnyGetter
    public Map<String, Object> getAttributeMap() {
        return attributeMap;
    }
}
