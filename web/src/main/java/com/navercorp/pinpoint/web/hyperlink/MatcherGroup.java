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

package com.navercorp.pinpoint.web.hyperlink;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author minwoo.jung
 */
public abstract class MatcherGroup {

    final List<ServerMatcher> serverMatcherList = new ArrayList<>();
    ServerMatcher defaultMatcher = new EmptyLinkMatcher();

    public MatcherGroup() {
    }

    public void setDefaultMatcher(ServerMatcher defaultMatcher) {
        this.defaultMatcher = Objects.requireNonNull(defaultMatcher, "defaultMatcher");
    }
    
    public ServerMatcher getDefaultMatcher() {
        return this.defaultMatcher;
    }

    public void addServerMatcher(ServerMatcher serverMatcher) {
        Objects.requireNonNull(serverMatcher, "serverMatcher");

        serverMatcherList.add(serverMatcher);
    }
    
    public void addMatcherGroup(MatcherGroup MatcherGroup) {
        serverMatcherList.addAll(MatcherGroup.getServerMatcherList());
        defaultMatcher = MatcherGroup.getDefaultMatcher();
    }
    
    public List<ServerMatcher> getServerMatcherList() {
        return serverMatcherList;
    }
    
    public HyperLink makeLinkInfo(LinkSource linkSource) {
        ServerMatcher serverMatcher = defaultMatcher;
        String value = getMatchingSource(linkSource);

        Objects.requireNonNull(value, "value");


        for (ServerMatcher matcher : serverMatcherList) {
            if (matcher.isMatched(value)) {
                serverMatcher = matcher;
            }
        }

        return serverMatcher.getLinkInfo(value);
    }

    abstract protected String getMatchingSource(LinkSource linkSource);

    abstract public boolean isMatchingType(LinkSource linkSource);
}