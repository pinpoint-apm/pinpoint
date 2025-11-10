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

package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * class for application in node map
 *
 * @author netspider
 * @author emeroad
 * @author HyunGil Jeong
 */
public class Node {

    private final Application application;

    // avoid NPE
    private ServerGroupList serverGroupList = ServerGroupList.empty();

    private NodeHistogram nodeHistogram;

    private boolean authorized = true;

    public Node(Application application) {
        this.application = Objects.requireNonNull(application, "application");
    }

    public String getApplicationTextName() {
        if (application.getServiceType().isUser()) {
            return "USER";
        } else {
            return application.getName();
        }
    }


    // TODO remove setter
    public void setServerGroupList(ServerGroupList serverGroupList) {
        this.serverGroupList = Objects.requireNonNull(serverGroupList, "serverGroupList");
    }

    @Nullable
    public ServerGroupList getServerGroupList() {
        return serverGroupList;
    }


    public Application getApplication() {
        return application;
    }

    public NodeName getNodeName() {
        return NodeName.of(application);
    }

    public String getNodeKey() {
        return NodeName.toNodeKey(application.getName(), application.getServiceType());
    }

    public ServiceType getServiceType() {
        return application.getServiceType();
    }

    @Nullable
    public NodeHistogram getNodeHistogram() {
        return nodeHistogram;
    }

    public void setNodeHistogram(NodeHistogram nodeHistogram) {
        this.nodeHistogram = nodeHistogram;
    }

    public ApdexScore getApdexScore() {
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        return ApdexScore.newApdexScore(histogram);
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    @Override
    public String toString() {
        return "Node [" + application + "]";
    }

}
