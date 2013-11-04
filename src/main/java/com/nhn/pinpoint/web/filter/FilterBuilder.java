package com.nhn.pinpoint.web.filter;

/**
 * @author emeroad
 */
public interface FilterBuilder {
    Filter build(String filterText);
}
