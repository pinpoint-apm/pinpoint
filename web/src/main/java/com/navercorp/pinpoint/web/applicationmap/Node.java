/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.view.NodeSerializer;
import com.navercorp.pinpoint.web.vo.Application;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class for application in node map
 *
 * @author netspider
 * @author emeroad
 */
@JsonSerialize(using = NodeSerializer.class)
public class Node {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String NODE_DELIMITER = "^";

    private Application application;
    // avoid NPE
    private ServerInstanceList serverInstanceList = new ServerInstanceList();

    private NodeHistogram nodeHistogram;
    
    private boolean authorized = true;
    
    public Node(Application application) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        this.application = application;
    }

    public Node(Node copyNode) {
        if (copyNode == null) {
            throw new NullPointerException("copyNode must not be null");
        }
        this.application = copyNode.application;
    }

    public String getApplicationTextName() {
        if (application.getServiceType().isUser()) {
            return "USER";
        } else {
            return application.getName();
        }
    }

    // TODO remove setter
    public void setServerInstanceList(ServerInstanceList serverInstanceList) {
        if (serverInstanceList == null) {
            throw new NullPointerException("serverInstanceList must not be null");
        }
        this.serverInstanceList = serverInstanceList;
    }

    public ServerInstanceList getServerInstanceList() {
        return serverInstanceList;
    }


    public Application getApplication() {
        return application;
    }


    public String getNodeName() {
        return application.getName() + NODE_DELIMITER + application.getServiceType();
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
    
    public void setApplication(Application application) {
        this.application = application;
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
