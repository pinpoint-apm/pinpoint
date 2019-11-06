/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class LinkedCallTree implements CallTree {
    private CallTreeNode root;

    public LinkedCallTree(final Align align) {
        this.root = new CallTreeNode(null, align);
    }

    public void update(final CallTree callTree) {
        final CallTreeNode updateNode = callTree.getRoot();
        this.root.setChild(updateNode.getChild());
        updateNode.setParent(this.root.getParent());
        this.root.setAlign(updateNode.getAlign());
    }

    public void updateForMultipleChild(final CallTree callTree) {
        final CallTreeNode newCallTreeNode = changeNodeToVirtualNode(callTree);
        final SpanBo spanBo = newCallTreeNode.getAlign().getSpanBo();

        // already has child
        if (root.hasChild()) {
            CallTreeNode last = getLastChild(root);
            last.setSibling(newCallTreeNode);

            SpanBo rootSpanBo = root.getAlign().getSpanBo();
            if (rootSpanBo.getStartTime() > spanBo.getStartTime()) {
                rootSpanBo.setStartTime(spanBo.getStartTime());
            }
        } else {
            SpanAlign spanAlign = createMultiChildSpanAlign(spanBo.getServiceType(), spanBo.getStartTime());

            this.root.setAlign(spanAlign);
            this.root.setChild(newCallTreeNode);
        }
    }

    private CallTreeNode getLastChild(CallTreeNode rootNode) {
        Objects.requireNonNull(rootNode, "rootNode");

        CallTreeNode prevSibling = null;
        CallTreeNode nextSibling = root.getChild();
        while (nextSibling != null) {
            prevSibling = nextSibling;
            nextSibling = prevSibling.getSibling();
        }

        return prevSibling;
    }

    private SpanAlign createMultiChildSpanAlign(short serviceType, long startTime) {
        SpanBo spanBo = new SpanBo();
        spanBo.setTransactionId(new TransactionId("UNKNOWN", 0, 0));
        spanBo.setServiceType(serviceType);
        spanBo.setStartTime(startTime);

        AnnotationBo multiChildAnnotation = createMultiChildAnnotation();

        List<AnnotationBo> annotationBoList = new ArrayList<>();
        annotationBoList.add(multiChildAnnotation);

        spanBo.setAnnotationBoList(annotationBoList);

        return new SpanAlign(spanBo, true);
    }

    private AnnotationBo createMultiChildAnnotation() {
        ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo("UNKNOWN", 0, -1);
        apiMetaDataBo.setApiInfo("Multi Child");
        apiMetaDataBo.setLineNumber(-1);
        apiMetaDataBo.setMethodTypeEnum(MethodTypeEnum.INVOCATION);

        return new AnnotationBo(AnnotationKey.API_METADATA.getCode(), apiMetaDataBo);
    }

    private CallTreeNode changeNodeToVirtualNode(final CallTree originalCallTree) {
        CallTreeNode originalNode = originalCallTree.getRoot();
        CallTreeNode newCallTreeNode = new CallTreeNode(this.root, originalNode.getAlign());

        CallTreeNode child = originalNode.getChild();
        newCallTreeNode.setChild(child);

        child.setParent(newCallTreeNode);

        return newCallTreeNode;
    }

    public void remove() {
        final CallTreeNode parent = this.root.getParent();
        CallTreeNode prev = null;
        CallTreeNode node = parent.getChild();
        while (node != null) {
            if (node == this.root) {
                CallTreeNode next = node.getSibling();
                if (prev == null) {
                    // first sibling
                    if (next == null) {
                        // only one.
                        parent.setChild((CallTreeNode) null);
                        this.root.setParent(null);
                    } else {
                        // copy to next
                        parent.setChild(next);
                        this.root.setSibling((CallTreeNode) null);
                        this.root.setParent(null);
                    }
                } else {
                    if (next == null) {
                        // last sibling
                        prev.setSibling((CallTreeNode) null);
                        this.root.setParent(null);
                    } else {
                        // copy to next
                        prev.setSibling(next);
                        this.root.setSibling((CallTreeNode) null);
                        this.root.setParent(null);
                    }
                }
                return;
            }
            // searching
            prev = node;
            node = node.getSibling();
        }
    }

    @Override
    public CallTreeNode getRoot() {
        return root;
    }

    @Override
    public CallTreeIterator iterator() {
        return new CallTreeIterator(root);
    }

    @Override
    public boolean isEmpty() {
        return root.getAlign() == null;
    }

    @Override
    public void add(CallTree tree) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int parentDepth, CallTree tree) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int depth, Align align) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort() {
    }
}