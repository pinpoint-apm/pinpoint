package com.nhn.pinpoint.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author emeroad
 */
public class UnwrapRootJackson2ObjectMapper extends ObjectMapper {
    public UnwrapRootJackson2ObjectMapper() {
        super();
        this.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    }
}
