package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.web.vo.Application;

/**
 * @author emeroad
 */
public interface LinkFilter {
    boolean filter(Application foundApplication);
}
