package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author emeroad
 */
public interface Limiter {
    void limit(long from, long to);

    void limit(Range range);
}
