package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NodeList implements Iterable<Node> {

    private final static Comparator<Node> STARTTIME_COMPARATOR
            = Comparator.comparingLong((Node node) -> node.getSpanBo().getStartTime());

    private final List<Node> nodeList;

    public static NodeList newNodeList(List<SpanBo> spans) {
        Objects.requireNonNull(spans, "spans");

        List<Node> list = spans.stream()
                .map(Node::toNode)
                .sorted(STARTTIME_COMPARATOR)
                .collect(Collectors.toList());
        return new NodeList(list);
    }

    public NodeList() {
        this.nodeList = new ArrayList<>();
    }

    public NodeList(List<Node> nodeList) {
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
        if (CollectionUtils.isEmpty(nodeList)) {
            return new NodeList();
        }
        Objects.requireNonNull(filter, "filter");

        List<Node> list = nodeList.stream()
                .filter(filter)
                .collect(Collectors.toList());
        return new NodeList(list);
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
                return node.getSpanCallTree().hasFocusSpan(filter);
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
