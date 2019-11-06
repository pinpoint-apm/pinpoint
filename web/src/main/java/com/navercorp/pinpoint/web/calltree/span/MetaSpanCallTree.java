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

/**
 * @author jaehong.kim
 */
public class MetaSpanCallTree extends SpanCallTree {

    public MetaSpanCallTree(Align align) {
        super(align);
    }

    @Override
    public void add(final CallTree tree) {
        final CallTreeNode node = tree.getRoot();
        if (node == null) {
            // skip
            return;
        }
        // increase elapsed time
        final int elapsedTime = getRoot().getAlign().getSpanBo().getElapsed() + node.getAlign().getSpanBo().getElapsed();
        getRoot().getAlign().getSpanBo().setElapsed(elapsedTime);
        super.add(tree);
    }
}