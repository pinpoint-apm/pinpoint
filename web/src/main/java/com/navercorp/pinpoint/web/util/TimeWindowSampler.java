package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.vo.Range;

/**
 * @author emeroad
 */
public interface TimeWindowSampler {
    long getWindowSize(Range range);
}
