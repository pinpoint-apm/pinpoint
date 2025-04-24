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
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeName;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.view.ServerGroupListView;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;
import java.util.Objects;

public class ServerHistogramView {
    private final String key;
    private final List<HistogramView> agentHistogramList;
    private final ServerGroupListView serverGroupListView;

    public static ServerHistogramView view(NodeHistogramSummary summary, HyperLinkFactory hyperLinkFactory) {
        Application application = summary.getApplication();
        String key = NodeName.toNodeName(application.getName(), application.getServiceType());
        List<HistogramView> agentHistogramList = summary.getNodeHistogram().createAgentHistogramViewList();
        ServerGroupList serverGroupList = summary.getServerGroupList();
        ServerGroupListView serverGroupListView = new ServerGroupListView(serverGroupList, hyperLinkFactory);
        return new ServerHistogramView(key, agentHistogramList, serverGroupListView);
    }

    public ServerHistogramView(String key, List<HistogramView> agentHistogramList, ServerGroupListView serverGroupListView) {
        this.key = Objects.requireNonNull(key, "key");
        this.agentHistogramList = Objects.requireNonNull(agentHistogramList, "agentHistogramList");
        this.serverGroupListView = Objects.requireNonNull(serverGroupListView, "serverGroupListView");
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
    public ServerGroupListView getServerList() {
        return serverGroupListView;
    }
}
