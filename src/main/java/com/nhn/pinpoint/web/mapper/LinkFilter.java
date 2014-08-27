package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.web.vo.Application;

/**
 * @author emeroad
 */
public interface LinkFilter {
    boolean filter(Application foundApplication);
}
