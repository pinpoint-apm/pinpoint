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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 */
public class NodeList {

    public static final NodeList EMPTY = new NodeList(Map.of());

    private final Map<Application, Node> nodeMap;

    public static NodeList of(Node node) {
        Objects.requireNonNull(node, "node");
        return new NodeList(Map.of(node.getApplication(), node));
    }

    public static NodeList of() {
        return EMPTY;
    }

    NodeList(Map<Application, Node> nodeMap) {
        this.nodeMap = Objects.requireNonNull(nodeMap, "nodeMap");
    }

    public Collection<Node> getNodeList() {
        return this.nodeMap.values();
    }

    public Node findNode(Application application) {
        Objects.requireNonNull(application, "application");
        return this.nodeMap.get(application);
    }

    public boolean contains(Application application) {
        Objects.requireNonNull(application, "application");
        return nodeMap.containsKey(application);
    }

    public int size() {
        return this.nodeMap.size();
    }

    public boolean isEmpty() {
        return this.nodeMap.isEmpty();
    }

    public static NodeList.Builder newBuilder() {
        return newBuilder(16);
    }

    public static NodeList.Builder newBuilder(int size) {
        return new NodeList.Builder(size);
    }

    public static class Builder {
        private final Map<Application, Node> nodeMap;

        Builder(int size) {
            this.nodeMap = new HashMap<>(size);
        }

        public boolean addNode(Node node) {
            Objects.requireNonNull(node, "node");

            final Application nodeId = node.getApplication();
            return nodeMap.putIfAbsent(nodeId, node) == null;
        }

        public boolean containsNode(Application application) {
            Objects.requireNonNull(application, "application");
            return nodeMap.containsKey(application);
        }

        public int size() {
            return this.nodeMap.size();
        }

        public NodeList build() {
            if (this.nodeMap.isEmpty()) {
                return EMPTY;
            }
            // return new NodeList(Map.copyOf(this.nodeMap));
            return new NodeList(this.nodeMap);
        }
    }
}
