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

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 *
 */
public class LinkInfo {
    private String linkName;
    private String linkUrl;
    public LinkType linkType;
    
    LinkInfo(String linkName, String linkUrl, LinkType linkType) {
        this.linkName = linkName;
        this.linkUrl = linkUrl;
        this.linkType = linkType;
    }

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }
    
    public String getLinkUrl() {
        return linkUrl;
    }
    
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
    
    public String getLinktype() {
        return linkType.getName();
    }
    
    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }
    
    public enum LinkType {
        ATAG("aTag"),
        BUTTON("button");
        
        private String name;
        
        LinkType(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
}