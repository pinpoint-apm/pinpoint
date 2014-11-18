package com.nhn.pinpoint.bootstrap.config;

import com.nhn.pinpoint.exception.PinpointException;

import java.util.Properties;

/**
 * @author emeroad
 */
public class PlaceHolderParser {

    private static final String PLACE_HOLDER_BEGIN = "${";
    private static final String PLACE_HOLDER_END = "}";

    public String parsePlaceHolderKey(String key, Properties properties) {
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        if (properties == null) {
            throw new NullPointerException("properties must not be null");
        }
        final StringBuilder buffer = new StringBuilder();
        int startIndex = 0;
        final int begin = key.indexOf(PLACE_HOLDER_BEGIN, startIndex);
        if (begin == -1) {
            return key;
        }
        final int end = key.indexOf(PLACE_HOLDER_END, begin);
        if (end == -1) {
            return null;
        }

        final String placeHolderKey = key.substring(begin+2, end);
        String placeHolderValue = properties.getProperty(placeHolderKey);
        if (placeHolderValue == null) {
            throw new PinpointException(PLACE_HOLDER_BEGIN + placeHolderKey + PLACE_HOLDER_END + " not found");
        }
        buffer.append(key, startIndex, begin - PLACE_HOLDER_BEGIN.length());
        buffer.append(placeHolderValue);
//        buffer.append(key, end + PLACE_HOLDER_END.length());

        return placeHolderKey;
    }
}
