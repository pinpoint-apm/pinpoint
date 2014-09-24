package com.nhn.pinpoint.web.applicationmap.link;

/**
 * @author emeroad
 */
public interface ServerMatcher {
    boolean isMatched(String serverName);

    String getLinkName();

    String getLink(String serverName);
}
