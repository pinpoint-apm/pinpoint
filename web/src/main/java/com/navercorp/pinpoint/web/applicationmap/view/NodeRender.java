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

package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;

import java.util.Objects;

public interface NodeRender {
    NodeView render(Node node);

    static NodeRender detailedRender(TimeHistogramFormat format, HyperLinkFactory hyperLinkFactory) {
        return new DefaultNodeRender(
                ApplicationTimeSeriesHistogramNodeView.detailedView(format),
                ServerListNodeView.detailedView(hyperLinkFactory),
                AgentHistogramNodeView.detailedView(),
                AgentTimeSeriesHistogramNodeView.detailedView(format));
    }

    static NodeRender forServerMap() {
        return new DefaultNodeRender(
                ApplicationTimeSeriesHistogramNodeView.detailedView(TimeHistogramFormat.V3),
                ServerListNodeView.emptyView(),
                AgentHistogramNodeView.emptyView(),
                AgentTimeSeriesHistogramNodeView.emptyView());
    }


    record DefaultNodeRender(ApplicationTimeSeriesHistogramNodeView applicationTimeSeriesHistogramNodeView,
                                    ServerListNodeView serverListNodeView, AgentHistogramNodeView agentHistogramNodeView,
                                    AgentTimeSeriesHistogramNodeView agentTimeSeriesHistogramNodeView) implements NodeRender {

        public DefaultNodeRender {
            Objects.requireNonNull(applicationTimeSeriesHistogramNodeView, "applicationTimeSeriesHistogramNodeView");
            Objects.requireNonNull(serverListNodeView, "serverListNodeView");
            Objects.requireNonNull(agentHistogramNodeView, "agentHistogramNodeView");
            Objects.requireNonNull(agentTimeSeriesHistogramNodeView, "agentTimeSeriesHistogramNodeView");
        }


        @Override
        public NodeView render(Node node) {
            return new NodeView(node,
                    applicationTimeSeriesHistogramNodeView,
                    serverListNodeView,
                    agentHistogramNodeView,
                    agentTimeSeriesHistogramNodeView);
        }

    }
}
