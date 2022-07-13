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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 *
 */
public class LinkInfo {
    private final String linkName;
    private final String linkUrl;
    private final LinkType linkType;
    
    public LinkInfo(String linkName, String linkUrl, LinkType linkType) {
        this.linkName = linkName;
        this.linkUrl = linkUrl;
        this.linkType = linkType;
    }

    @JsonProperty("linkName")
    public String getLinkName() {
        return linkName;
    }

    @JsonProperty("linkURL")
    public String getLinkUrl() {
        return linkUrl;
    }


    @JsonProperty("linkType")
    public String getLinkType() {
        return linkType.getName();
    }

    @Override
    public String toString() {
        return "LinkInfo{" +
                "linkName='" + linkName + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                ", linkType=" + linkType +
                '}';
    }

    public enum LinkType {
        ATAG("aTag"),
        BUTTON("button");
        
        private final String name;
        
        LinkType(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
}