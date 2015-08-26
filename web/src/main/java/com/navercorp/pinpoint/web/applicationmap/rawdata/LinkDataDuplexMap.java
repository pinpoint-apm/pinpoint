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

package com.navercorp.pinpoint.web.applicationmap.rawdata;

import com.navercorp.pinpoint.web.vo.LinkKey;

import java.util.Collection;

/**
 * @author emeroad
 */
public class LinkDataDuplexMap {

    private final LinkDataMap sourceLinkDataMap;

    private final LinkDataMap targetLinkDataMap;

    public LinkDataDuplexMap() {
        this.sourceLinkDataMap = new LinkDataMap();
        this.targetLinkDataMap = new LinkDataMap();
    }


    public LinkDataMap getSourceLinkDataMap() {
        return sourceLinkDataMap;
    }

    public Collection<LinkData> getSourceLinkDataList() {
        return sourceLinkDataMap.getLinkDataList();
    }

    public LinkDataMap getTargetLinkDataMap() {
        return targetLinkDataMap;
    }

    public Collection<LinkData> getTargetLinkDataList() {
        return targetLinkDataMap.getLinkDataList();
    }


    public void addLinkDataDuplexMap(LinkDataDuplexMap linkDataDuplexMap) {
        if (linkDataDuplexMap == null) {
            throw new NullPointerException("linkDataDuplexMap must not be null");
        }
        for (LinkData copyLinkData : linkDataDuplexMap.sourceLinkDataMap.getLinkDataList()) {
            addSourceLinkData(copyLinkData);
        }
        for (LinkData copyLinkData : linkDataDuplexMap.targetLinkDataMap.getLinkDataList()) {
            addTargetLinkData(copyLinkData);
        }
    }

    public void addSourceLinkData(LinkData copyLinkData) {
        if (copyLinkData == null) {
            throw new NullPointerException("copyLinkData must not be null");
        }
        sourceLinkDataMap.addLinkData(copyLinkData);
    }


    public void addTargetLinkData(LinkData copyLinkData) {
        if (copyLinkData == null) {
            throw new NullPointerException("copyLinkData must not be null");
        }
        targetLinkDataMap.addLinkData(copyLinkData);
    }


    public int size() {
        return sourceLinkDataMap.size() + targetLinkDataMap.size();
    }


    public LinkData getSourceLinkData(LinkKey findLinkKey) {
        if (findLinkKey == null) {
            throw new NullPointerException("findLinkKey must not be null");
        }

        return sourceLinkDataMap.getLinkData(findLinkKey);
    }

    public LinkData getTargetLinkData(LinkKey findLinkKey) {
        if (findLinkKey == null) {
            throw new NullPointerException("findLinkKey must not be null");
        }

        return targetLinkDataMap.getLinkData(findLinkKey);
    }

    public long getTotalCount() {
        return this.sourceLinkDataMap.getTotalCount() + this.targetLinkDataMap.getTotalCount();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LinkDataDuplexMap{");
        sb.append("sourceLinkDataMap=").append(sourceLinkDataMap);
        sb.append(", targetLinkDataMap=").append(targetLinkDataMap);
        sb.append('}');
        return sb.toString();
    }
}
