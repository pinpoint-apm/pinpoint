package com.navercorp.pinpoint.web.applicationmap.link;

/**
 * @author emeroad
 */
public interface ServerMatcher {
    boolean isMatched(String serverName);

    String getLinkName();

    String getLink(String serverName);
}
