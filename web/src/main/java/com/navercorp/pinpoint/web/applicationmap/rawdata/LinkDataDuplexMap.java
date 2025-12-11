/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.rawdata;

import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    public List<Application> getSourceToApplication() {
        Collection<LinkData> linkDataList = sourceLinkDataMap.getLinkDataList();
        List<Application> list = new ArrayList<>(linkDataList.size());
        for (LinkData linkData : linkDataList) {
            list.add(linkData.getToApplication());
        }
        return list;
    }

    public Collection<LinkData> getSourceLinkDataList() {
        return sourceLinkDataMap.getLinkDataList();
    }

    public LinkDataMap getTargetLinkDataMap() {
        return targetLinkDataMap;
    }

    public List<Application> getTargetFromApplication() {
        Collection<LinkData> linkDataList = targetLinkDataMap.getLinkDataList();
        List<Application> list = new ArrayList<>(linkDataList.size());
        for (LinkData linkData : linkDataList) {
            list.add(linkData.getFromApplication());
        }
        return list;
    }

    public Collection<LinkData> getTargetLinkDataList() {
        return targetLinkDataMap.getLinkDataList();
    }


    public void addLinkDataDuplexMap(LinkDataDuplexMap linkDataDuplexMap) {
        Objects.requireNonNull(linkDataDuplexMap, "linkDataDuplexMap");

        for (LinkData copyLinkData : linkDataDuplexMap.sourceLinkDataMap.getLinkDataList()) {
            addSourceLinkData(copyLinkData);
        }
        for (LinkData copyLinkData : linkDataDuplexMap.targetLinkDataMap.getLinkDataList()) {
            addTargetLinkData(copyLinkData);
        }
    }

    public void addSourceLinkData(LinkData copyLinkData) {
        Objects.requireNonNull(copyLinkData, "copyLinkData");

        sourceLinkDataMap.addLinkData(copyLinkData);
    }


    public void addTargetLinkData(LinkData copyLinkData) {
        Objects.requireNonNull(copyLinkData, "copyLinkData");
        targetLinkDataMap.addLinkData(copyLinkData);
    }


    public int size() {
        return sourceLinkDataMap.size() + targetLinkDataMap.size();
    }


    public LinkData getSourceLinkData(LinkKey findLinkKey) {
        Objects.requireNonNull(findLinkKey, "findLinkKey");

        return sourceLinkDataMap.getLinkData(findLinkKey);
    }

    public LinkData getTargetLinkData(LinkKey findLinkKey) {
        Objects.requireNonNull(findLinkKey, "findLinkKey");

        return targetLinkDataMap.getLinkData(findLinkKey);
    }

    public long getTotalCount() {
        return this.sourceLinkDataMap.getTotalCount() + this.targetLinkDataMap.getTotalCount();
    }

    @Override
    public String toString() {
        return "LinkDataDuplexMap{" +
                "sourceLinkDataMap=" + sourceLinkDataMap +
                ", targetLinkDataMap=" + targetLinkDataMap +
                '}';
    }
}
