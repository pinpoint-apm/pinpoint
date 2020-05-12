/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.applicationmap.link.LinkType;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeType;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SearchOption;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class MapServiceOption {
    private Application sourceApplication;
    private Range range;
    private SearchOption searchOption;
    private NodeType nodeType;
    private LinkType linkType;
    private boolean useStatisticsServerInstanceList;

    private MapServiceOption(Builder builder) {
        this.sourceApplication = builder.sourceApplication;
        this.range = builder.range;
        this.searchOption = builder.searchOption;
        this.nodeType = builder.nodeType;
        this.linkType = builder.linkType;
        this.useStatisticsServerInstanceList = builder.useStatisticsServerInstanceList;
    }

    public Application getSourceApplication() {
        return sourceApplication;
    }

    public Range getRange() {
        return range;
    }

    public SearchOption getSearchOption() {
        return searchOption;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public boolean isUseStatisticsServerInstanceList() {
        return useStatisticsServerInstanceList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MapServiceOption{");
        sb.append("sourceApplication=").append(sourceApplication);
        sb.append(", range=").append(range);
        sb.append(", searchOption=").append(searchOption);
        sb.append(", nodeType=").append(nodeType);
        sb.append(", linkType=").append(linkType);
        sb.append(", useStatisticsServerInstanceList=").append(useStatisticsServerInstanceList);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private Application sourceApplication;
        private Range range;
        private SearchOption searchOption;
        private NodeType nodeType;
        private LinkType linkType;
        // option
        boolean useStatisticsServerInstanceList;

        public Builder(Application sourceApplication, Range range, SearchOption searchOption, NodeType nodeType, LinkType linkType) {
            this.sourceApplication = Objects.requireNonNull(sourceApplication, "sourceApplication");
            this.range = Objects.requireNonNull(range,"range");
            this.searchOption = Objects.requireNonNull(searchOption, "searchOption");
            this.nodeType = Objects.requireNonNull(nodeType, "nodeType");
            this.linkType = Objects.requireNonNull(linkType, "linkType");
        }

        public Builder setUseStatisticsServerInstanceList(boolean useStatisticsServerInstanceList) {
            this.useStatisticsServerInstanceList = useStatisticsServerInstanceList;
            return this;
        }

        public MapServiceOption build() {
            return new MapServiceOption(this);
        }
    }
}