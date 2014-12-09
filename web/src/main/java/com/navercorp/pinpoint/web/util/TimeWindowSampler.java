package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author emeroad
 */
public interface TimeWindowSampler {
    long getWindowSize(Range range);
}
