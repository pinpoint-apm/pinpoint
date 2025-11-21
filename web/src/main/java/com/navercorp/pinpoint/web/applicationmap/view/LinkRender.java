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

import com.navercorp.pinpoint.web.applicationmap.link.Link;

import java.util.Objects;

public interface LinkRender {
    LinkView render(Link link);


    static LinkRender forServerMap() {
        return new DefaultLinkRender(
                ApplicationTimeSeriesHistogramLinkView.detailedView(TimeHistogramView.TimeseriesHistogram),
                AgentLinkView.emptyView());
    }

    static LinkRender detailedRender(TimeHistogramView timeHistogramView) {
        return new DefaultLinkRender(
                ApplicationTimeSeriesHistogramLinkView.detailedView(timeHistogramView),
                AgentLinkView.detailedView(timeHistogramView));
    }

    record DefaultLinkRender(ApplicationTimeSeriesHistogramLinkView applicationTimeSeriesHistogramLinkView,
                                    AgentLinkView agentLinkView) implements LinkRender {

        public DefaultLinkRender {
            Objects.requireNonNull(applicationTimeSeriesHistogramLinkView, "applicationTimeSeriesHistogramLinkView");
            Objects.requireNonNull(agentLinkView, "agentLinkView");
        }

        @Override
        public LinkView render(Link link) {
            return new LinkView(link, applicationTimeSeriesHistogramLinkView, agentLinkView);
        }
    }
}
