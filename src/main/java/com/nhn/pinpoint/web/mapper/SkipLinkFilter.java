package com.nhn.pinpoint.web.mapper;

/**
 * @author emeroad
 */
public class SkipLinkFilter implements LinkFilter {
    @Override
    public boolean filter(String foundApplicationName, short foundServiceType) {
        return false;
    }
}
