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

import com.navercorp.pinpoint.web.applicationmap.link.LinkInfo.LinkType;

/**
 * @author minwoo.jung
 */
public class PostfixServerMatcher implements ServerMatcher {

    private final String postfix;
    private final String url;
    private final String linkName;
    private final LinkType linkType;
    
    public PostfixServerMatcher(String postfix, String url, String linkName, LinkType linkType) {
        this.postfix = postfix;
        this.url = url;
        this.linkName = linkName;
        this.linkType = linkType;
    }
    
    @Override
    public boolean isMatched(String value) {
        if (value == null) {
            return false;
        }
        return value.endsWith(postfix);
    }

    private String getLink(String value) {
        final int index = value.lastIndexOf(postfix);
        
        if (index == -1) {
            throw new IllegalArgumentException("invalid serverName:" + value);
        }
        
        String hostName = value.substring(0, index);
        return url + hostName;
    }


    @Override
    public LinkInfo getLinkInfo(String value) {
        return new LinkInfo(linkName, getLink(value), linkType);
    }
}