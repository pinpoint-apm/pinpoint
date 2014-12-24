/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.link;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 * @author minwoo.jung
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
    
    public ServerMatcher match(String value) {
        if (value == null) {
            throw new NullPointerException("serverName must not be null");
        }

        for (ServerMatcher serverMatcher : serverMatcherList) {
            if(serverMatcher.isMatched(value)) {
                return serverMatcher;
            }
        }
        return defaultMatcher;
    }

}
