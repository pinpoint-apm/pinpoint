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

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanAligner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();

    public static final int INIT_MATCH = -1;
    // not matched
    public static final int ERROR_MATCH = 0;
    // transaction completed successfully
    public static final int COMPLETE_MATCH = 1;
    // transaction in-flight or missing data
    public static final int PROGRESS_MATCH = 2;

    private int matchType = INIT_MATCH;
    private final long collectorAcceptTime;
    private final List<Node> sortedNodeList = new ArrayList<>();
    private final Map<String, Node> spanToLinkMap = new HashMap<>();
    private final List<Link> linkList = new ArrayList<>();
    private final MetaSpanCallTreeFactory metaSpanCallTreeFactory = new MetaSpanCallTreeFactory();

    public SpanAligner(final List<SpanBo> spans, final long collectorAcceptTime) {
        // init sorted node list
        for (SpanBo span : spans) {
            final Node node = new Node(span, new SpanCallTree(new SpanAlign(span)));
            this.sortedNodeList.add(node);
        }

        // sort
        this.sortedNodeList.sort(new Comparator<Node>() {
            @Override
            public int compare(Node first, Node second) {
                return (int) (first.span.getStartTime() - second.span.getStartTime());
            }
        });

        // for performance & remove duplicate span
        final List<Node> duplicatedNodeList = new ArrayList<>();
        for (Node node : this.sortedNodeList) {
            final String key = node.span.getParentSpanId() + "." + node.span.getSpanId();
            // check duplicated span
            final Node value = this.spanToLinkMap.get(key);
            if (value == null) {
                this.spanToLinkMap.put(key, node);
            } else {
                updateMatchType(PROGRESS_MATCH);
                // duplicated span, choose focus span
                if (node.span.getCollectorAcceptTime() == collectorAcceptTime) {
                    // replace value
                    this.spanToLinkMap.put(key, node);
                    duplicatedNodeList.add(value);
                    logger.warn("Duplicated span - choose focus {}", node);
                } else {
                    // add remove list
                    duplicatedNodeList.add(node);
                    logger.warn("Duplicated span - ignored second {}", node);
                }
            }
        }
        // clean duplicated node
        this.sortedNodeList.removeAll(duplicatedNodeList);
        this.collectorAcceptTime = collectorAcceptTime;
    }

    private void updateMatchType(final int matchType) {
        if (this.matchType == INIT_MATCH) {
            this.matchType = matchType;
        }
    }

    public int getMatchType() {
        return matchType;
    }

    public CallTree align() {
        // populate call tree
        populate();
        // link to span
        link();
        // fill missing span
        fill();
        // remove unlinked
        clear();
        // select root
        return root();
    }

    private void populate() {
        for (Node node : this.sortedNodeList) {
            final List<SpanEventBo> spanEventBoList = node.span.getSpanEventBoList();
            final SpanAsyncEventMap asyncSpanEventMap = extractAsyncSpanEvent(spanEventBoList);
            if (isDebug) {
                logger.debug("Populate span {parentSpanId={}, spanId={}, startTime={}, root={}, eventSize={}, asyncEventSize={}}", node.span.getParentSpanId(), node.span.getSpanId(), node.span.getStartTime(), node.span.isRoot(), spanEventBoList.size(), asyncSpanEventMap.size());
            }
            populateSpanEvent(node, node.spanCallTree, spanEventBoList, asyncSpanEventMap);
        }
    }

    SpanAsyncEventMap extractAsyncSpanEvent(final List<SpanEventBo> spanEventBoList) {
        final SpanAsyncEventMap spanAsyncEventMap = new SpanAsyncEventMap();
        if (spanEventBoList == null) {
            return spanAsyncEventMap;
        }

        final List<SpanEventBo> removeList = new ArrayList<>();
        for (SpanEventBo spanEvent : spanEventBoList) {
            if (spanAsyncEventMap.add(spanEvent)) {
                removeList.add(spanEvent);
            }
        }
        spanAsyncEventMap.sort();
        // clear async span event
        spanEventBoList.removeAll(removeList);
        return spanAsyncEventMap;
    }

    private void populateSpanEvent(final Node node, final SpanCallTree spanCallTree, final List<SpanEventBo> spanEventBoList, SpanAsyncEventMap asyncSpanEventMap) {
        if (spanEventBoList == null) {
            return;
        }

        // cursor tree
        SpanCallTree tree = spanCallTree;
        for (SpanEventBo spanEventBo : spanEventBoList) {
            if (isDebug) {
                logger.debug("Populate spanEvent{seq={}, depth={}, async={}, event={}}", spanEventBo.getSequence(), spanEventBo.getDepth(), spanEventBo.isAsync(), spanEventBo);
            }
            final SpanAlign spanEventAlign = new SpanAlign(node.span, spanEventBo);
            try {
                if (!node.corrupted) {
                    tree.add(spanEventBo.getDepth(), spanEventAlign);
                }
            } catch (CorruptedSpanCallTreeNodeException e) {
                logger.warn("Corrupted span event {}", e.getMessage());
                node.corrupted = true;
                updateMatchType(PROGRESS_MATCH);

                final long startTimeMillis = node.span.getStartTime() + spanEventBo.getStartElapsed();
                final SpanCallTree corruptedCallTree = metaSpanCallTreeFactory.corrupted(e.getTitle(), node.span.getParentSpanId(), node.span.getSpanId(), startTimeMillis);
                tree.add(corruptedCallTree);
                // replace cursor tree.
                tree = corruptedCallTree;
            }
            // link
            final long nextSpanId = spanEventBo.getNextSpanId();
            if (nextSpanId != -1) {
                // add linked call tree
                final LinkedCallTree linkedCallTree = new LinkedCallTree(new SpanAlign(new SpanBo()));
                tree.add(linkedCallTree);
                final long startTimeMillis = node.span.getStartTime() + spanEventBo.getStartElapsed();
                final Link link = new Link(node.span.getParentSpanId(), node.span.getSpanId(), nextSpanId, linkedCallTree, startTimeMillis);
                this.linkList.add(link);
            }
            // async
            final int nextAsyncId = spanEventBo.getNextAsyncId();
            for (List<SpanEventBo> list : asyncSpanEventMap.get(nextAsyncId)) {
                populateAsyncSpanEvent(node, tree, list, asyncSpanEventMap);
            }
        }
    }

    private void populateAsyncSpanEvent(final Node node, final SpanCallTree callTree, final List<SpanEventBo> asyncSpanEventBoList, final SpanAsyncEventMap asyncSpanEventMap) {
        if (node.corrupted) {
            // populate current call tree
            populateSpanEvent(node, callTree, asyncSpanEventBoList, asyncSpanEventMap);
        } else {
            // populate new call tree
            final SpanAlign spanAlign = new SpanAlign(node.span);
            final SpanAsyncCallTree spanAsyncCallTree = new SpanAsyncCallTree(spanAlign);
            populateSpanEvent(node, spanAsyncCallTree, asyncSpanEventBoList, asyncSpanEventMap);
            callTree.add(spanAsyncCallTree);
        }
    }

    private void link() {
        final List<Link> linkedList = new ArrayList<>();
        for (Link link : this.linkList) {
            final String key = link.spanId + "." + link.nextSpanId;
            final Node node = this.spanToLinkMap.get(key);
            if (node != null) {
                if (isDebug) {
                    logger.debug("Linked link {} to node {}", link, node);
                }
                // linked
                node.linked = true;
                link.linked = true;
                link.linkedCallTree.update(node.spanCallTree);
                linkedList.add(link);
            }
        }
        // remove linked
        this.linkList.removeAll(linkedList);
    }

    private void fill() {
        final List<Node> unlinkedNodeList = NodeList.filterUnlinked(this.sortedNodeList);
        for (Node node : unlinkedNodeList) {
            if (node.linked || node.span.isRoot()) {
                continue;
            }
            // find missing grand parent.
            final List<Link> targetLinkList = LinkList.filterSpan(this.linkList, node.span);
            if (CollectionUtils.hasLength(targetLinkList)) {
                final Link matchedLink = LinkList.matchSpan(targetLinkList, node.span);
                if (matchedLink != null) {
                    if (isDebug) {
                        logger.debug("Fill link {} to node {}", matchedLink, node);
                    }
                    final CallTree unknownSpanCallTree = this.metaSpanCallTreeFactory.unknown(node.span.getStartTime());
                    unknownSpanCallTree.add(node.spanCallTree);
                    node.linked = true;
                    matchedLink.linkedCallTree.update(unknownSpanCallTree);
                    matchedLink.linked = true;
                    // clear
                    this.linkList.remove(matchedLink);
                    updateMatchType(PROGRESS_MATCH);
                }
            }
        }
    }

    private void clear() {
        for (Link link : this.linkList) {
            if (!link.linked) {
                link.linkedCallTree.remove();
            }
        }
    }

    private CallTree root() {
        if (this.sortedNodeList.isEmpty()) {
            // WARNING what wrong ?
            logger.warn("Not found span, node list is empty");
            updateMatchType(PROGRESS_MATCH);
            return this.metaSpanCallTreeFactory.unknown(0);
        }

        final List<Node> unlinkedNodeList = NodeList.filterUnlinked(this.sortedNodeList);
        if (unlinkedNodeList.isEmpty()) {
            // WARNING recursive link ?
            logger.warn("Not found top node, node list={}", Arrays.asList(this.sortedNodeList));
            updateMatchType(PROGRESS_MATCH);
            return this.metaSpanCallTreeFactory.unknown(0);
        } else if (unlinkedNodeList.size() == 1) {
            // best matching
            return selectFirstSpan(unlinkedNodeList);
        }
        return selectJustSpan(unlinkedNodeList);
    }

    // best
    private CallTree selectFirstSpan(List<Node> topNodeList) {
        final Node node = topNodeList.get(0);
        if (node.spanCallTree.isRootSpan()) {
            updateMatchType(COMPLETE_MATCH);
            return node.spanCallTree;
        }

        if (isInfo) {
            logger.info("Select span in top node list, not found root span");
        }
        CallTree rootCallTree = this.metaSpanCallTreeFactory.unknown(node.span.getStartTime());
        rootCallTree.add(node.spanCallTree);
        updateMatchType(PROGRESS_MATCH);
        return rootCallTree;
    }

    // just do it
    private CallTree selectJustSpan(List<Node> topNodeList) {
        // multiple spans
        if (isDebug) {
            logger.debug("Multiple top node list. size={}", topNodeList.size());
            for (Node node : topNodeList) {
                logger.debug("  node={}", node);
            }
        }

        // find root
        final List<Node> rootNodeList = NodeList.filterRoot(topNodeList);
        if (rootNodeList.size() >= 1) {
            return selectInRootNodeList(rootNodeList);
        }

        // find focus
        final List<Node> focusNodeList = NodeList.filterFocus(topNodeList, this.collectorAcceptTime);
        if (focusNodeList.size() >= 1) {
            return selectInFocusNodeList(focusNodeList, topNodeList);
        }

        if (isInfo) {
            logger.info("Select first span in top node list, not found root & focus span");
        }
        final Node node = topNodeList.get(0);
        updateMatchType(PROGRESS_MATCH);
        return selectInNodeList(node, topNodeList);
    }

    private CallTree selectInRootNodeList(final List<Node> rootNodeList) {
        // in root list
        if (rootNodeList.size() == 1) {
            if (isInfo) {
                logger.info("Select root span in top node list");
            }
            final Node node = rootNodeList.get(0);
            updateMatchType(PROGRESS_MATCH);
            return node.spanCallTree;
        }
        // find focus
        final List<Node> focusNodeList = NodeList.filterFocus(rootNodeList, this.collectorAcceptTime);
        if (focusNodeList.size() == 1) {
            if (isInfo) {
                logger.info("Select root & focus span in top node list");
            }
            final Node node = focusNodeList.get(0);
            updateMatchType(PROGRESS_MATCH);
            return node.spanCallTree;
        } else if (focusNodeList.size() > 1) {
            if (isInfo) {
                logger.info("Select first root & focus span in top node list");
            }
            final Node node = focusNodeList.get(0);
            updateMatchType(PROGRESS_MATCH);
            return node.spanCallTree;
        }
        // not found focus
        if (isInfo) {
            logger.info("Select first root span in top node list, not found focus span");
        }
        final Node node = rootNodeList.get(0);
        updateMatchType(PROGRESS_MATCH);
        return node.spanCallTree;
    }

    private CallTree selectInFocusNodeList(final List<Node> focusNodeList, final List<Node> topNodeList) {
        if (focusNodeList.size() == 1) {
            if (isInfo) {
                logger.info("Select focus span in top node list, not found root span");
            }
            final Node node = focusNodeList.get(0);
            updateMatchType(PROGRESS_MATCH);
            return selectInNodeList(node, topNodeList);
        }
        if (isInfo) {
            logger.info("Select first focus span in top node list, not found root span");
        }
        final Node node = focusNodeList.get(0);
        updateMatchType(PROGRESS_MATCH);
        return selectInNodeList(node, topNodeList);
    }

    private CallTree selectInNodeList(final Node node, final List<Node> topNodeList) {
        final CallTree unknownCallTree = this.metaSpanCallTreeFactory.unknown(node.span.getStartTime());
        unknownCallTree.add(node.spanCallTree);
        // find same parent
        final List<Node> sameParentNodeList = NodeList.filterParent(topNodeList, node.span.getParentSpanId());
        sameParentNodeList.remove(node);
        for (Node siblingNode : sameParentNodeList) {
            unknownCallTree.add(siblingNode.spanCallTree);
        }
        return unknownCallTree;
    }

    private static class Node {
        private SpanBo span;
        private SpanCallTree spanCallTree;
        private boolean linked = false;
        private boolean corrupted = false;

        public Node(final SpanBo span, final SpanCallTree spanCallTree) {
            this.span = span;
            this.spanCallTree = spanCallTree;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("parentSpanId=").append(span.getParentSpanId());
            sb.append(", spanId=").append(span.getSpanId());
            sb.append(", startTime=").append(span.getStartTime());
            sb.append(", elapsed=").append(span.getElapsed());
            sb.append(", linked=").append(linked);
            sb.append('}');
            return sb.toString();
        }
    }

    private static class NodeList {
        private static List<Node> filterUnlinked(List<Node> nodeList) {
            return filter(nodeList, new NodeFilter() {
                @Override
                public boolean filter(Node node) {
                    return !node.linked;
                }
            });
        }

        private static List<Node> filterRoot(List<Node> nodeList) {
            return filter(nodeList, new NodeFilter() {
                @Override
                public boolean filter(Node node) {
                    return node.span.isRoot();
                }
            });
        }

        private static List<Node> filterFocus(List<Node> nodeList, final long collectorAcceptTime) {
            return filter(nodeList, new NodeFilter() {
                @Override
                public boolean filter(Node node) {
                    return node.spanCallTree.hasFocusSpan(collectorAcceptTime);
                }
            });
        }

        private static List<Node> filterParent(List<Node> nodeList, final long parentSpanId) {
            return filter(nodeList, new NodeFilter() {
                @Override
                public boolean filter(Node node) {
                    return parentSpanId == node.span.getParentSpanId();
                }
            });
        }

        private static List<Node> filter(List<Node> nodeList, NodeFilter filter) {
            if (CollectionUtils.isEmpty(nodeList)) {
                return Collections.emptyList();
            }

            final List<Node> result = new ArrayList<>();
            for (Node node : nodeList) {
                if (filter.filter(node)) {
                    result.add(node);
                }
            }
            return result;
        }
    }

    private interface NodeFilter {
        boolean filter(final Node node);
    }

    private static class Link {
        private long parentSpanId;
        private long spanId;
        private long nextSpanId;
        private LinkedCallTree linkedCallTree;
        private boolean linked;
        private long startTimeMillis;

        public Link(final long parentSpanId, final long spanId, final long nextSpanId, final LinkedCallTree linkedCallTree, final long startTimeMillis) {
            this.parentSpanId = parentSpanId;
            this.spanId = spanId;
            this.nextSpanId = nextSpanId;
            this.linkedCallTree = linkedCallTree;
            this.startTimeMillis = startTimeMillis;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("parentSpanId=").append(parentSpanId);
            sb.append(", spanId=").append(spanId);
            sb.append(", nextSpanId=").append(nextSpanId);
            sb.append(", linked=").append(linked);
            sb.append(", startTimeMillis=").append(startTimeMillis);
            sb.append('}');
            return sb.toString();
        }
    }

    private static class LinkList {
        private static List<Link> filterSpan(final List<Link> linkList, final SpanBo span) {
            if (CollectionUtils.isEmpty(linkList)) {
                return Collections.emptyList();
            }

            final List<Link> result = new ArrayList<>();
            for (Link link : linkList) {
                if (span.getParentSpanId() == link.parentSpanId && span.getSpanId() == link.spanId) {
                    // skip self's link
                    continue;
                }
                if (link.nextSpanId == span.getParentSpanId()) {
                    result.add(link);
                }
            }
            return result;
        }

        private static Link matchSpan(final List<Link> linkList, final SpanBo span) {
            if (CollectionUtils.isEmpty(linkList)) {
                return null;
            }

            linkList.sort(new Comparator<Link>() {
                @Override
                public int compare(Link first, Link second) {
                    return (int) (first.startTimeMillis - second.startTimeMillis);
                }
            });

            for (Link link : linkList) {
                if (link.startTimeMillis <= span.getStartTime()) {
                    return link;
                }
            }
            return null;
        }
    }
}