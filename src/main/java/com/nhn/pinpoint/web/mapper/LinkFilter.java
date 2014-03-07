package com.nhn.pinpoint.web.mapper;

/**
 * @author emeroad
 */
public interface LinkFilter {
    boolean filter(String foundApplicationName, short foundServiceType);
}
