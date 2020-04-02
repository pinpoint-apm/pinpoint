/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.view.NodeHistogramSummarySerializer;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = NodeHistogramSummarySerializer.class)
public class NodeHistogramSummary {

    private final ServerInstanceList serverInstanceList;
    private final NodeHistogram nodeHistogram;

    public NodeHistogramSummary(ServerInstanceList serverInstanceList, NodeHistogram nodeHistogram) {
        this.serverInstanceList = Objects.requireNonNull(serverInstanceList, "serverInstanceList");
        this.nodeHistogram = Objects.requireNonNull(nodeHistogram, "nodeHistogram");
    }

    public ServerInstanceList getServerInstanceList() {
        return serverInstanceList;
    }

    public NodeHistogram getNodeHistogram() {
        return nodeHistogram;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NodeHistogramSummary{");
        sb.append("serverInstanceList=").append(serverInstanceList);
        sb.append(", nodeHistogram=").append(nodeHistogram);
        sb.append('}');
        return sb.toString();
    }
}
