package com.nhn.pinpoint.web.applicationmap.link;

/**
 * @author emeroad
 */
public class NSightMatcher implements ServerMatcher {

    public static final String POST_FIX = ".nhnsystem.com";

    public static final String URL = "http://nsight.nhncorp.com/dashboard_server/";

    @Override
    public boolean isMatched(String serverName) {
        if (serverName == null) {
            return false;
        }
        return serverName.endsWith(POST_FIX);
    }

    @Override
    public String getLinkName() {
        return "NSight";
    }

    @Override
    public String getLink(String serverName) {

        final int index = serverName.lastIndexOf(POST_FIX);
        if (index == -1) {
            throw new IllegalArgumentException("invalid serverName:" + serverName);
        }
        String hostName = serverName.substring(0, index);
        return URL + hostName;
    }
}
