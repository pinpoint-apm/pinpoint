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

import com.navercorp.pinpoint.web.vo.Application;

import java.util.*;

/**
 * @author emeroad
 */
public class NodeList {

    private final Map<Application, Node> nodeMap = new HashMap<>();

    public Collection<Node> getNodeList() {
        return this.nodeMap.values();
    }

    public Node findNode(Application application) {
        Objects.requireNonNull(application, "application");

        return this.nodeMap.get(application);
    }

    public boolean addNode(Node node) {
        Objects.requireNonNull(node, "node");

        final Application nodeId = node.getApplication();
        Node findNode = findNode(nodeId);
        if (findNode != null) {
            return false;
        }
        return nodeMap.put(nodeId, node) == null;
    }


    public void addNodeList(NodeList nodeList) {
        Objects.requireNonNull(nodeList, "nodeList");

        for (Node node : nodeList.getNodeList()) {
            addNode(node);
        }
    }

    public boolean containsNode(Application application) {
        Objects.requireNonNull(application, "application");

        return nodeMap.containsKey(application);
    }

    public int size() {
        return this.nodeMap.size();
    }
}
