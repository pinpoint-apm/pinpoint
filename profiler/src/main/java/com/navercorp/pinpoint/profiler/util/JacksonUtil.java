package com.navercorp.pinpoint.profiler.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.PinpointConstants;

import java.util.Map;

/**
 * @Author: wangj881
 * @Description:
 * @Date: create in 2022/12/4 9:18
 */
public class JacksonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JacksonUtil() {
    }

    public static String toJsonString(Object object) throws JsonProcessingException {
        if (null == object) {
            return PinpointConstants.EMPTY_STRING;
        }
        return OBJECT_MAPPER.writeValueAsString(object);

    }

    public static Map parseObjectToMap(Object object) {
        if (null == object) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(object, Map.class);
    }

}
