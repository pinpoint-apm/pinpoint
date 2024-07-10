package com.navercorp.pinpoint.common.server.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MapResponse extends SimpleResponse {
    private Map<String, Object> attributeMap;

    public static Response ok() {
        return new MapResponse(Result.SUCCESS);
    }

    public MapResponse(Result result, String message) {
        super(result, message);
    }

    public MapResponse(Result result) {
        super(result);
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
