/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.view.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeName;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;

import java.util.List;
import java.util.Objects;

public class ServerHistogramView {
    private final String key;
    private final List<HistogramView> agentHistogramList;
    private final ServerGroupList serverGroupList;

    public ServerHistogramView(String key, List<HistogramView> agentHistogramList, ServerGroupList serverGroupList) {
        this.key = Objects.requireNonNull(key, "key");
        this.agentHistogramList = Objects.requireNonNull(agentHistogramList, "agentHistogramList");
        this.serverGroupList = Objects.requireNonNull(serverGroupList, "serverGroupList");
    }

    public ServerHistogramView(NodeName nodeName, NodeHistogram nodeHistogram, ServerGroupList serverGroupList) {
        this(nodeName.getName(), nodeHistogram.createAgentHistogramViewList(), serverGroupList);
    }

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @JsonProperty("serverHistogramList")
    public List<HistogramView> getServerHistogramData() {
        return agentHistogramList;
    }

    @JsonProperty("serverList")
    public ServerGroupList getServerList() {
        return serverGroupList;
    }
}
