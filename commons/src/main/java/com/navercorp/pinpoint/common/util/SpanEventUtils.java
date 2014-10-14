package com.nhn.pinpoint.common.util;

import com.nhn.pinpoint.thrift.dto.TSpanEvent;


/**
 * @author emeroad
 */
public class SpanEventUtils {

    public static boolean hasException(TSpanEvent spanEvent) {
        if (spanEvent.isSetExceptionInfo()) {
            return true;
        }
        return false;
    }
}
