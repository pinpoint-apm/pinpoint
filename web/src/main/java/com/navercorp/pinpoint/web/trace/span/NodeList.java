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

package com.navercorp.pinpoint.web.trace.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NodeList implements Iterable<Node> {

    private final static Comparator<Node> STARTTIME_COMPARATOR
            = Comparator.comparingLong((Node node) -> node.getSpanBo().getStartTime());

    public static final NodeList EMPTY = new NodeList(Collections.emptyList());

    private final List<Node> nodeList;

    public static NodeList newNodeList(List<SpanBo> spans) {
        Objects.requireNonNull(spans, "spans");
        if (spans.isEmpty()) {
            return EMPTY;
        }

        List<Node> list = new ArrayList<>(spans.size());
        for (SpanBo span : spans) {
            list.add(Node.toNode(span));
        }
        list.sort(STARTTIME_COMPARATOR);
        return new NodeList(list);
    }

    public static NodeList of(List<Node> nodeList) {
        Objects.requireNonNull(nodeList, "nodeList");
        if (CollectionUtils.isEmpty(nodeList)) {
            return EMPTY;
        }
        return new NodeList(nodeList);
    }

    private NodeList(List<Node> nodeList) {
        this.nodeList = Objects.requireNonNull(nodeList, "nodeList");
    }

    @Override
    public Iterator<Node> iterator() {
        return this.nodeList.iterator();
    }

    @Override
    public void forEach(Consumer<? super Node> action) {
        Objects.requireNonNull(action, "action");

        this.nodeList.forEach(action);
    }

    public boolean remove(Node node) {
        return this.nodeList.remove(node);
    }

    public boolean removeAll(Collection<Node> list) {
        return this.nodeList.removeAll(list);
    }

    public boolean isEmpty() {
        return this.nodeList.isEmpty();
    }

    public int size() {
        return this.nodeList.size();
    }

    public Node get(int index) {
        return this.nodeList.get(index);
    }


    public NodeList filter(Predicate<Node> filter) {
        Objects.requireNonNull(filter, "filter");
        if (nodeList.isEmpty()) {
            return EMPTY;
        }

        List<Node> list = new ArrayList<>();
        for (Node node : nodeList) {
            if (filter.test(node)) {
                list.add(node);
            }
        }
        return NodeList.of(list);
    }


    public static Predicate<Node> unlinkFilter() {
        return new Predicate<Node>() {
            @Override
            public boolean test(Node node) {
                return !node.isLinked();
            }
        };
    }

    public static Predicate<Node> rootFilter() {
        return new Predicate<Node>() {
            @Override
            public boolean test(Node node) {
                return node.getSpanBo().isRoot();
            }
        };
    }

    public static Predicate<Node> callTreeFilter(Predicate<SpanBo> filter) {
        Objects.requireNonNull(filter, "filter");

        return new Predicate<Node>() {
            @Override
            public boolean test(Node node) {
                return node.getSpanCallTree().filterSpan(filter);
            }

            @Override
            public String toString() {
                return "callTreeFilter:" + filter;
            }
        };
    }

    public static Predicate<Node> parentFilter(final long parentSpanId) {
        return new Predicate<Node>() {
            @Override
            public boolean test(Node node) {
                return parentSpanId == node.getSpanBo().getParentSpanId();
            }

            @Override
            public String toString() {
                return "parentSpanId:" + parentSpanId;
            }
        };
    }


}
