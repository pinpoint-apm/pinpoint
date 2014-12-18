package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.thrift.dto.TSpanEvent;


/**
 * @author emeroad
 */
public final class SpanEventUtils {

    private SpanEventUtils() {
    }

    public static boolean hasException(TSpanEvent spanEvent) {
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }
        if (spanEvent.isSetExceptionInfo()) {
            return true;
        }
        return false;
    }
}
