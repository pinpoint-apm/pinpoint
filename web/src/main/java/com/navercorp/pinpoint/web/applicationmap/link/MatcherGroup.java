package com.nhn.pinpoint.web.applicationmap.link;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class MatcherGroup {

    private final List<ServerMatcher> serverMatcherList = new ArrayList<ServerMatcher>();
    private ServerMatcher defaultMatcher = new DefaultNSightMatcher();

    public MatcherGroup() {
        setAddDefaultMatcher();
    }

    public void setDefaultMatcher(ServerMatcher defaultMatcher) {
        if (defaultMatcher == null) {
            throw new NullPointerException("defaultMatcher must not be null");
        }
        this.defaultMatcher = defaultMatcher;
    }

    public void addServerMatcher(ServerMatcher serverMatcher) {
        if (serverMatcher == null) {
            throw new NullPointerException("serverMatcher must not be null");
        }
        serverMatcherList.add(serverMatcher);
    }

    public void setAddDefaultMatcher() {
        // 시간되면 spring에서 찾아 오게 하던지??
        // 그냥 빨랑 할려고 코드로 함.
        serverMatcherList.add(new NSightMatcher());
        serverMatcherList.add(new JapanNSightMatcher());
    }

    public ServerMatcher match(String serverName) {
        if (serverName == null) {
            throw new NullPointerException("serverName must not be null");
        }

        for (ServerMatcher serverMatcher : serverMatcherList) {
            if(serverMatcher.isMatched(serverName)) {
                return serverMatcher;
            }
        }
        return defaultMatcher;
    }

}
