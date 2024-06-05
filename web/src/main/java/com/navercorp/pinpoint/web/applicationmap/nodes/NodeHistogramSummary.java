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

import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class NodeHistogramSummary {

    private final Application application;
    private final ServerGroupList serverGroupList;
    private final NodeHistogram nodeHistogram;

    public NodeHistogramSummary(Application application, ServerGroupList serverGroupList, NodeHistogram nodeHistogram) {
        this.application = Objects.requireNonNull(application, "application");
        this.serverGroupList = Objects.requireNonNull(serverGroupList, "serverGroupList");
        this.nodeHistogram = Objects.requireNonNull(nodeHistogram, "nodeHistogram");
    }

    public Application getApplication() {
        return application;
    }

    public ServerGroupList getServerGroupList() {
        return serverGroupList;
    }

    public NodeHistogram getNodeHistogram() {
        return nodeHistogram;
    }

    public Histogram getHistogram() {
        return nodeHistogram.getApplicationHistogram();
    }

    public ApplicationTimeHistogram getApplicationTimeHistogram() {
        return nodeHistogram.getApplicationTimeHistogram();
    }

    @Override
    public String toString() {
        return "NodeHistogramSummary{" + "serverGroupList=" + serverGroupList +
                ", nodeHistogram=" + nodeHistogram +
                '}';
    }
}
