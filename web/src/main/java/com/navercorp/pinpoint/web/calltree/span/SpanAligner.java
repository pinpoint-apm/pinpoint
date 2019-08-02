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
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanAligner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceState traceState = new TraceState();

    private final long collectorAcceptTime;
    private final List<Node> nodeList;
    private final LinkMap linkMap;
    private final List<Link> linkList = new ArrayList<>();
    private final MetaSpanCallTreeFactory metaSpanCallTreeFactory = new MetaSpanCallTreeFactory();
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    public SpanAligner(final List<SpanBo> spans, final long collectorAcceptTime, ServiceTypeRegistryService serviceTypeRegistryService) {
        this.nodeList = Node.newNodeList(spans);
        this.linkMap = LinkMap.buildLinkMap(nodeList, traceState, collectorAcceptTime, serviceTypeRegistryService);
        removeDuplicateNode();
        this.collectorAcceptTime = collectorAcceptTime;
        this.serviceTypeRegistryService = serviceTypeRegistryService;
    }

    private void removeDuplicateNode() {
        // TODO??
        List<Node> duplicatedNodeList = linkMap.getDuplicatedNodeList();
        nodeList.removeAll(duplicatedNodeList);
        duplicatedNodeList.clear();
    }

    public TraceState.State getMatchType() {
        return traceState.getState();
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
        for (Node node : this.nodeList) {
            if (isDebug) {
                SpanBo span = node.getSpanBo();
                List<Align> alignList = node.getAlignList();
                SpanAsyncEventMap asyncSpanEventMap = node.getAsyncSpanEventMap();
                logger.debug("Populate span {parentSpanId={}, spanId={}, startTime={}, root={}, eventSize={}, asyncEventSize={}}", span.getParentSpanId(), span.getSpanId(), span.getStartTime(), span.isRoot(), alignList.size(), asyncSpanEventMap.size());
            }
            populateSpanEvent(node, node.getSpanCallTree(), node.getAlignList());
        }
    }

    private void populateSpanEvent(final Node node, final SpanCallTree spanCallTree, final List<Align> alignList) {
        if (CollectionUtils.isEmpty(alignList)) {
            return;
        }

        // cursor tree
        SpanCallTree tree = spanCallTree;
        for (Align align : alignList) {
            try {
                if (!node.isCorrupted()) {
                    tree.add(align);
                }
            } catch (CorruptedSpanCallTreeNodeException e) {
                logger.warn("Corrupted span event {}", e.getMessage(), e);
                node.setCorrupted(true);
                traceState.progress();

                final long startTimeMillis = align.getStartTime();
                final SpanBo spanBo = node.getSpanBo();
                final SpanCallTree corruptedCallTree = metaSpanCallTreeFactory.corrupted(e.getTitle(), spanBo.getParentSpanId(), spanBo.getSpanId(), startTimeMillis);
                tree.add(corruptedCallTree);
                // replace cursor tree.
                tree = corruptedCallTree;
            }
            // link
            final long nextSpanId = align.getSpanEventBo().getNextSpanId();
            if (nextSpanId != -1) {
                // add linked call tree
                final LinkedCallTree linkedCallTree = new LinkedCallTree(new SpanAlign(new SpanBo()));
                tree.add(linkedCallTree);
                final Link link = Link.newLink(align, linkedCallTree);
                this.linkList.add(link);
            }

            // async
            final int nextAsyncId = align.getSpanEventBo().getNextAsyncId();
            final SpanAsyncEventMap asyncSpanEventMap = node.getAsyncSpanEventMap();

            for (List<Align> asyncAlignList : asyncSpanEventMap.getAsyncAlign(nextAsyncId)) {
                populateAsyncSpanEvent(node, tree, asyncAlignList);
            }
        }
    }


    private void populateAsyncSpanEvent(final Node node, final SpanCallTree callTree, final List<Align> alignList) {
        if (node.isCorrupted()) {
            // populate current call tree
            populateSpanEvent(node, callTree, alignList);
        } else {
            // populate new call tree
            final Align align = new SpanAlign(node.getSpanBo());
            final SpanAsyncCallTree spanAsyncCallTree = new SpanAsyncCallTree(align);
            populateSpanEvent(node, spanAsyncCallTree, alignList);
            callTree.add(spanAsyncCallTree);
        }
    }

    private void link() {
        final List<Link> linkedList = new ArrayList<>();
        for (Link link : this.linkList) {
            final List<Node> nodeList = this.linkMap.findNode(link);

            if (CollectionUtils.nullSafeSize(nodeList) > 1) {
                for (Node node : nodeList) {
                    if (putNodeToLink(link, node, true)) {
                        linkedList.add(link);
                    }
                }
            } else if (CollectionUtils.nullSafeSize(nodeList) == 1) {
                Node node = nodeList.get(0);
                if (putNodeToLink(link, node, false)) {
                    linkedList.add(link);
                }
            }
        }
        // remove linked
        this.linkList.removeAll(linkedList);
    }

    private boolean putNodeToLink(Link link, Node node, boolean hasMultipleChild) {
        if (node != null) {
            if (isDebug) {
                logger.debug("Linked link {} to node {}", link, node);
            }
            // linked
            node.setLinked(true);
            link.setLinked(true);
            if (hasMultipleChild) {
                link.getLinkedCallTree().updateForMultipleChild(node.getSpanCallTree());
            } else {
                link.getLinkedCallTree().update(node.getSpanCallTree());
            }
            return true;
        }
        return false;
    }

    private void fill() {
        final List<Node> unlinkedNodeList = NodeList.filterUnlinked(this.nodeList);
        for (Node node : unlinkedNodeList) {
            final SpanBo spanBo = node.getSpanBo();
            if (node.isLinked() || spanBo.isRoot()) {
                continue;
            }
            // find missing grand parent.
            final List<Link> targetLinkList = LinkList.filterSpan(this.linkList, spanBo);
            if (CollectionUtils.hasLength(targetLinkList)) {
                final Link matchedLink = LinkList.matchSpan(targetLinkList, spanBo);
                if (matchedLink != null) {
                    if (isDebug) {
                        logger.debug("Fill link {} to node {}", matchedLink, node);
                    }
                    final CallTree unknownSpanCallTree = this.metaSpanCallTreeFactory.unknown(spanBo.getStartTime());
                    unknownSpanCallTree.add(node.getSpanCallTree());
                    node.setLinked(true);
                    matchedLink.getLinkedCallTree().update(unknownSpanCallTree);
                    matchedLink.setLinked(true);
                    // clear
                    this.linkList.remove(matchedLink);
                    traceState.progress();
                }
            }
        }
    }

    private void clear() {
        for (Link link : this.linkList) {
            if (!link.isLinked()) {
                link.getLinkedCallTree().remove();
            }
        }
    }

    private CallTree root() {
        if (this.nodeList.isEmpty()) {
            // WARNING what wrong ?
            logger.warn("Not found span, node list is empty");
            traceState.progress();
            return this.metaSpanCallTreeFactory.unknown(0);
        }

        final List<Node> unlinkedNodeList = NodeList.filterUnlinked(this.nodeList);
        if (unlinkedNodeList.isEmpty()) {
            // WARNING recursive link ?
            logger.warn("Not found top node, node list={}", this.nodeList);
            traceState.progress();
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
        if (node.getSpanCallTree().isRootSpan()) {
            traceState.complete();
            return node.getSpanCallTree();
        }

        logger.info("Select span in top node list, not found root span");

        CallTree rootCallTree = this.metaSpanCallTreeFactory.unknown(node.getSpanBo().getStartTime());
        rootCallTree.add(node.getSpanCallTree());
        traceState.progress();
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

        logger.info("Select first span in top node list, not found root & focus span");

        final Node node = topNodeList.get(0);
        traceState.progress();
        return selectInNodeList(node, topNodeList);
    }

    private CallTree selectInRootNodeList(final List<Node> rootNodeList) {
        // in root list
        if (rootNodeList.size() == 1) {
            logger.info("Select root span in top node list");

            final Node node = rootNodeList.get(0);
            traceState.progress();
            return node.getSpanCallTree();
        }
        // find focus
        final List<Node> focusNodeList = NodeList.filterFocus(rootNodeList, this.collectorAcceptTime);
        if (focusNodeList.size() == 1) {
            logger.info("Select root & focus span in top node list");
            final Node node = focusNodeList.get(0);
            traceState.progress();
            return node.getSpanCallTree();
        } else if (focusNodeList.size() > 1) {
            logger.info("Select first root & focus span in top node list");
            final Node node = focusNodeList.get(0);
            traceState.progress();
            return node.getSpanCallTree();
        }
        // not found focus
        logger.info("Select first root span in top node list, not found focus span");
        final Node node = rootNodeList.get(0);
        traceState.progress();
        return node.getSpanCallTree();
    }

    private CallTree selectInFocusNodeList(final List<Node> focusNodeList, final List<Node> topNodeList) {
        if (focusNodeList.size() == 1) {
            logger.info("Select focus span in top node list, not found root span");
            final Node node = focusNodeList.get(0);
            traceState.progress();
            return selectInNodeList(node, topNodeList);
        }

        logger.info("Select first focus span in top node list, not found root span");

        final Node node = focusNodeList.get(0);
        traceState.progress();
        return selectInNodeList(node, topNodeList);
    }

    private CallTree selectInNodeList(final Node node, final List<Node> topNodeList) {
        final SpanBo spanBo = node.getSpanBo();
        final CallTree unknownCallTree = this.metaSpanCallTreeFactory.unknown(spanBo.getStartTime());
        unknownCallTree.add(node.getSpanCallTree());
        // find same parent
        final List<Node> sameParentNodeList = NodeList.filterParent(topNodeList, spanBo.getParentSpanId());
        sameParentNodeList.remove(node);
        for (Node siblingNode : sameParentNodeList) {
            unknownCallTree.add(siblingNode.getSpanCallTree());
        }
        return unknownCallTree;
    }

        private static class NodeList {
        private static List<Node> filterUnlinked(List<Node> nodeList) {
            return filter(nodeList, new NodeFilter() {
                @Override
                public boolean filter(Node node) {
                    return !node.isLinked();
                }
            });
        }

        private static List<Node> filterRoot(List<Node> nodeList) {
            return filter(nodeList, new NodeFilter() {
                @Override
                public boolean filter(Node node) {
                    return node.getSpanBo().isRoot();
                }
            });
        }

        private static List<Node> filterFocus(List<Node> nodeList, final long collectorAcceptTime) {
            return filter(nodeList, new NodeFilter() {
                @Override
                public boolean filter(Node node) {
                    return node.getSpanCallTree().hasFocusSpan(collectorAcceptTime);
                }
            });
        }

        private static List<Node> filterParent(List<Node> nodeList, final long parentSpanId) {
            return filter(nodeList, new NodeFilter() {
                @Override
                public boolean filter(Node node) {
                    return parentSpanId == node.getSpanBo().getParentSpanId();
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

    private static class LinkList {
        private static List<Link> filterSpan(final List<Link> linkList, final SpanBo span) {
            if (CollectionUtils.isEmpty(linkList)) {
                return Collections.emptyList();
            }

            final List<Link> result = new ArrayList<>();
            for (Link link : linkList) {
                if (span.getParentSpanId() == link.getParentSpanId() && span.getSpanId() == link.getSpanId()) {
                    // skip self's link
                    continue;
                }
                if (link.getNextSpanId() == span.getParentSpanId()) {
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
                    return (int) (first.getStartTimeMillis() - second.getStartTimeMillis());
                }
            });

            for (Link link : linkList) {
                if (link.getStartTimeMillis() <= span.getStartTime()) {
                    return link;
                }
            }
            return null;
        }
    }

}