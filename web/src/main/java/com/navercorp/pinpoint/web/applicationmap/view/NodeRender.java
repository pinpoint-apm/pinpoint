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

import com.navercorp.pinpoint.web.applicationmap.config.MapProperties;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;

import java.util.Objects;

public interface NodeRender {
    NodeView render(Node node);

    /**
     * Creates a detailed node render with the given time histogram view, hyper link factory, and map properties.
     * @param timeHistogramView the time histogram view
     * @param hyperLinkFactory the hyper link factory
     * @param mapProperties the map properties
     * @return a detailed node render
     */
    static NodeRender detailedRender(TimeHistogramView timeHistogramView,
                                     HyperLinkFactory hyperLinkFactory,
                                     MapProperties mapProperties) {
        return new DefaultNodeRender(
                ApplicationTimeSeriesHistogramNodeView.detailedView(timeHistogramView),
                ApplicationApdexScoreSlotView.detailedView(),
                ServerListNodeView.detailedView(hyperLinkFactory),
                AgentHistogramNodeView.detailedView(),
                AgentTimeSeriesHistogramNodeView.detailedView(timeHistogramView),
                mapProperties.isEnableServiceMap());
    }

    static NodeRender forServerMap() {
        return new DefaultNodeRender(
                ApplicationTimeSeriesHistogramNodeView.emptyView(),
                ApplicationApdexScoreSlotView.detailedView(),
                ServerListNodeView.emptyView(),
                AgentHistogramNodeView.emptyView(),
                AgentTimeSeriesHistogramNodeView.emptyView(),
                false);
    }

    static NodeRender forServiceMap() {
        return new DefaultNodeRender(
                ApplicationTimeSeriesHistogramNodeView.emptyView(),
                ApplicationApdexScoreSlotView.detailedView(),
                ServerListNodeView.emptyView(),
                AgentHistogramNodeView.emptyView(),
                AgentTimeSeriesHistogramNodeView.emptyView(),
                true);
    }


    record DefaultNodeRender(ApplicationTimeSeriesHistogramNodeView applicationTimeSeriesHistogramNodeView,
                             ApplicationApdexScoreSlotView applicationApdexScoreSlotView,
                             ServerListNodeView serverListNodeView,
                             AgentHistogramNodeView agentHistogramNodeView,
                             AgentTimeSeriesHistogramNodeView agentTimeSeriesHistogramNodeView,
                             boolean enableServiceMap) implements NodeRender {

        public DefaultNodeRender {
            Objects.requireNonNull(applicationTimeSeriesHistogramNodeView, "applicationTimeSeriesHistogramNodeView");
            Objects.requireNonNull(applicationApdexScoreSlotView, "applicationApdexScoreSlotView");
            Objects.requireNonNull(serverListNodeView, "serverListNodeView");
            Objects.requireNonNull(agentHistogramNodeView, "agentHistogramNodeView");
            Objects.requireNonNull(agentTimeSeriesHistogramNodeView, "agentTimeSeriesHistogramNodeView");
        }


        @Override
        public NodeView render(Node node) {
            return new NodeView(node,
                    applicationTimeSeriesHistogramNodeView,
                    applicationApdexScoreSlotView,
                    serverListNodeView,
                    agentHistogramNodeView,
                    agentTimeSeriesHistogramNodeView,
                    enableServiceMap);
        }

    }
}
