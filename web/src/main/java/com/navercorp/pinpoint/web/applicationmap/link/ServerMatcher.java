package com.navercorp.pinpoint.web.applicationmap.link;

/**
 * @author emeroad
 */
public interface ServerMatcher {
    boolean isMatched(String value);

    String getLinkName();

    String getLink(String value);
}
