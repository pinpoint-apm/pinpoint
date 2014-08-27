package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.vo.Range;

/**
 * @author emeroad
 */
public interface Limiter {
    void limit(long from, long to);

    void limit(Range range);
}
