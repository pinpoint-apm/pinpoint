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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.view.NodeSerializer;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * class for application in node map
 *
 * @author netspider
 * @author emeroad
 * @author HyunGil Jeong
 */
@JsonSerialize(using = NodeSerializer.class)
public class Node {

    private final NodeType nodeType;

    private final Application application;

    // avoid NPE
    private ServerGroupList serverGroupList = ServerGroupList.empty();

    private NodeHistogram nodeHistogram;

    private boolean authorized = true;
    private TimeHistogramFormat timeHistogramFormat = TimeHistogramFormat.V1;
    private boolean v3Format = false;

    public Node(Application application) {
        this(NodeType.DETAILED, application);
    }

    public Node(NodeType nodeType, Application application) {
        this.nodeType = Objects.requireNonNull(nodeType, "nodeType");
        this.application = Objects.requireNonNull(application, "application");
    }

    public Node(Node copyNode) {
        Objects.requireNonNull(copyNode, "copyNode");
        this.nodeType = copyNode.nodeType;
        this.application = copyNode.application;
    }

    public String getApplicationTextName() {
        if (application.getServiceType().isUser()) {
            return "USER";
        } else {
            return application.getName();
        }
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    // TODO remove setter
    public void setServerGroupList(ServerGroupList serverGroupList) {
        this.serverGroupList = Objects.requireNonNull(serverGroupList, "serverGroupList");
    }

    public ServerGroupList getServerGroupList() {
        return serverGroupList;
    }


    public Application getApplication() {
        return application;
    }

    public NodeName getNodeName() {
        return NodeName.of(application);
    }

    public ServiceType getServiceType() {
        return application.getServiceType();
    }

    public NodeHistogram getNodeHistogram() {
        return nodeHistogram;
    }

    public void setNodeHistogram(NodeHistogram nodeHistogram) {
        this.nodeHistogram = nodeHistogram;
    }

    public ApdexScore getApdexScore() {
        return ApdexScore.newApdexScore(nodeHistogram.getApplicationHistogram());
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public TimeHistogramFormat getTimeHistogramFormat() {
        return timeHistogramFormat;
    }

    public void setTimeHistogramFormat(TimeHistogramFormat timeHistogramFormat) {
        this.timeHistogramFormat = timeHistogramFormat;
    }

    public boolean isV3Format() {
        return v3Format;
    }

    public void setV3Format(boolean v3Format) {
        this.v3Format = v3Format;
    }

    @Override
    public String toString() {
        return "Node [" + application + "]";
    }

}
