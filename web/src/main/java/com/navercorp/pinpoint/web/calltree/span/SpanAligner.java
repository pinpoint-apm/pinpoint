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

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public class SpanAligner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceState traceState = new TraceState();

    private final Predicate<Node> focusFilter;

    private final NodeList nodeList;

    private final LinkList linkList;
    private final LinkMap linkMap;
    private final MetaSpanCallTreeFactory metaSpanCallTreeFactory = new MetaSpanCallTreeFactory();

    public SpanAligner(final List<SpanBo> spans, Predicate<SpanBo> filter, ServiceTypeRegistryService serviceTypeRegistryService) {
        this.nodeList = NodeList.newNodeList(spans);
        this.linkList = new LinkList();

        Objects.requireNonNull(filter, "filter");

        this.linkMap = LinkMap.buildLinkMap(nodeList, traceState, filter, serviceTypeRegistryService);
        removeDuplicateNode();

        this.focusFilter = NodeList.callTreeFilter(filter);
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
        LinkList filter = this.linkList.filter(this::usedLink);

        // remove linked
        this.linkList.removeAll(filter);
    }

    private boolean usedLink(Link link) {
        final List<Node> nodeList = this.linkMap.findNode(link);
        if (CollectionUtils.isEmpty(nodeList)) {
            return false;
        }
        int size = nodeList.size();
        if (size > 1) {
            for (Node node : nodeList) {
                if (putNodeToLink(link, node, true)) {
                    return true;
                }
            }
        } else if (size == 1) {
            Node node = nodeList.get(0);
            if (putNodeToLink(link, node, false)) {
                return true;
            }
        }
        return false;
    }

    private boolean putNodeToLink(Link link, Node node, boolean hasMultipleChild) {
        if (node == null) {
            return false;
        }
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

    private void fill() {
        final NodeList unlinkedNodeList = this.nodeList.filter(NodeList.unlinkFilter());
        for (Node node : unlinkedNodeList) {
            final SpanBo spanBo = node.getSpanBo();
            if (node.isLinked() || spanBo.isRoot()) {
                continue;
            }
            // find missing grand parent.
            final LinkList targetLinkList = this.linkList.filter(LinkList.spanFilter(spanBo));
            if (!targetLinkList.isEmpty()) {
                final Link matchedLink = targetLinkList.matchSpan(spanBo);
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

        // find root
        final NodeList rootNodeList = nodeList.filter(NodeList.rootFilter());
        if (rootNodeList.size() >= 1) {
            return selectInRootNodeList(rootNodeList);
        }

        // Corner case : root node not found
        final NodeList unlinkedNodeList = this.nodeList.filter(NodeList.unlinkFilter());
        logger.debug("unlinkNode {}/{}", unlinkedNodeList.size(), this.nodeList.size());
        if (unlinkedNodeList.isEmpty()) {
            // WARNING recursive link ?
            logger.warn("Not found top node, unlink={} node list={}", unlinkedNodeList.size(), this.nodeList);
            traceState.progress();
            return this.metaSpanCallTreeFactory.unknown(0);
        } else if (unlinkedNodeList.size() == 1) {
            // best matching
            return selectFirstSpan(unlinkedNodeList);
        }
        return selectJustSpan(unlinkedNodeList);
    }

    // best
    private CallTree selectFirstSpan(NodeList topNodeList) {
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
    private CallTree selectJustSpan(NodeList topNodeList) {
        // multiple spans
        logger.info("Multiple top node list. size={} focusFilter:{}", topNodeList.size(), this.focusFilter);
        if (isDebug) {
            topNodeList.forEach((Node node) -> logger.debug("  node={}", node));
        }

        // find focus
        final NodeList focusNodeList = topNodeList.filter(this.focusFilter);
        if (focusNodeList.size() >= 1) {
            return selectInFocusNodeList(focusNodeList, topNodeList);
        }

        logger.info("Select first span in top node list, not found root & focus span");

        final Node node = topNodeList.get(0);
        traceState.progress();
        return selectInNodeList(node, topNodeList);
    }

    private CallTree selectInRootNodeList(final NodeList rootNodeList) {
        // in root list
        if (rootNodeList.size() == 1) {
            logger.info("Select root span in top node list");

            final Node node = rootNodeList.get(0);
            traceState.progress();
            return node.getSpanCallTree();
        }
        // find focus
        final NodeList focusNodeList = rootNodeList.filter(this.focusFilter);
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

    private CallTree selectInFocusNodeList(final NodeList focusNodeList, final NodeList topNodeList) {
        if (focusNodeList.size() == 1) {
            logger.info("Select focus span in top node list, not found root span");
            final Node node = focusNodeList.get(0);
            traceState.progress();
            return selectInNodeList(node, topNodeList);
        }

        logger.info("Select first focus span in top node list, not found root span");
        // TODO
        final Node node = focusNodeList.get(0);
        traceState.progress();
        return selectInNodeList(node, topNodeList);
    }

    private CallTree selectInNodeList(final Node node, final NodeList topNodeList) {
        final SpanBo spanBo = node.getSpanBo();
        final CallTree unknownCallTree = this.metaSpanCallTreeFactory.unknown(spanBo.getStartTime());
        unknownCallTree.add(node.getSpanCallTree());
        // find same parent
        final NodeList sameParentNodeList = topNodeList.filter(NodeList.parentFilter(spanBo.getParentSpanId()));
        sameParentNodeList.remove(node);
        for (Node siblingNode : sameParentNodeList) {
            unknownCallTree.add(siblingNode.getSpanCallTree());
        }
        return unknownCallTree;
    }

}