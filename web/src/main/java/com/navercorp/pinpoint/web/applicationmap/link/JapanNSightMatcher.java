package com.nhn.pinpoint.web.applicationmap.link;

/**
 * @author emeroad
 */
public class JapanNSightMatcher implements ServerMatcher {

    public static final String POST_FIX = ".nhnjp.ism";

    // TODO 일본쪽 jsight 이름을 수정할것..
    public static final String URL = "http://nsight.nhncorp.jp/dashboard_server/";

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
