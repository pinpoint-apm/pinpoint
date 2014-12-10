package com.navercorp.pinpoint.web.applicationmap.link;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 *
 */
public class EmptyLinkMatcher implements ServerMatcher{

    @Override
    public boolean isMatched(String serverName) {
        return true;
    }

    @Override
    public String getLinkName() {
        return null;
    }

    @Override
    public String getLink(String serverName) {
        return null;
    }

}
