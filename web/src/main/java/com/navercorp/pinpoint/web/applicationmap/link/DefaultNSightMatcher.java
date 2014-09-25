package com.nhn.pinpoint.web.applicationmap.link;

/**
 * @author emeroad
 */
public class DefaultNSightMatcher implements ServerMatcher {

    @Override
    public boolean isMatched(String serverName) {
        return true;
    }

    @Override
    public String getLinkName() {
        return "NSight";
    }

    @Override
    public String getLink(String serverName) {
        return NSightMatcher.URL + serverName;
    }
}
