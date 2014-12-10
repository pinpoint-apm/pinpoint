package com.navercorp.pinpoint.web.applicationmap.link;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class MatcherGroup {

    private final List<ServerMatcher> serverMatcherList = new ArrayList<ServerMatcher>();
    private ServerMatcher defaultMatcher = new EmptyLinkMatcher();

    public MatcherGroup() {
    }

    public void setDefaultMatcher(ServerMatcher defaultMatcher) {
        if (defaultMatcher == null) {
            throw new NullPointerException("defaultMatcher must not be null");
        }
        this.defaultMatcher = defaultMatcher;
    }
    
    public ServerMatcher getDefaultMatcher() {
        return this.defaultMatcher;
    }

    public void addServerMatcher(ServerMatcher serverMatcher) {
        if (serverMatcher == null) {
            throw new NullPointerException("serverMatcher must not be null");
        }
        serverMatcherList.add(serverMatcher);
    }
    
    public void addMatcherGroup(MatcherGroup MatcherGroup) {
        serverMatcherList.addAll(MatcherGroup.getServerMatcherList());
        defaultMatcher = MatcherGroup.getDefaultMatcher();
    }
    
    public List<ServerMatcher> getServerMatcherList() {
        return serverMatcherList;
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
