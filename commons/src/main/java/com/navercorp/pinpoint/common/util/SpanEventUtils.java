package com.navercorp.pinpoint.common.util;

import com.navercorp.pinpoint.thrift.dto.TSpanEvent;


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
