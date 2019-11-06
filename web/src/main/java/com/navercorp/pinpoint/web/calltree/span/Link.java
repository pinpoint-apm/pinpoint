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

import com.navercorp.pinpoint.common.server.bo.SpanBo;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Link {
    private final long parentSpanId;
    private final long spanId;
    private final long nextSpanId;
    private final LinkedCallTree linkedCallTree;
    private final long startTimeMillis;

    private boolean linked;

    public static Link newLink(Align align, LinkedCallTree linkedCallTree) {
        Objects.requireNonNull(align, "align");
        Objects.requireNonNull(linkedCallTree, "linkedCallTree");

        final SpanBo spanBo = align.getSpanBo();
        final long startTimeMillis = align.getStartTime();
        final long nextSpanId = align.getSpanEventBo().getNextSpanId();
        return new Link(spanBo.getParentSpanId(), spanBo.getSpanId(), nextSpanId, linkedCallTree, startTimeMillis);
    }

    public Link(final long parentSpanId, final long spanId, final long nextSpanId, final LinkedCallTree linkedCallTree, final long startTimeMillis) {
        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
        this.nextSpanId = nextSpanId;
        this.linkedCallTree = Objects.requireNonNull(linkedCallTree, "linkedCallTree");
        this.startTimeMillis = startTimeMillis;
    }

    public long getParentSpanId() {
        return parentSpanId;
    }

    public long getSpanId() {
        return spanId;
    }

    public long getNextSpanId() {
        return nextSpanId;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public LinkedCallTree getLinkedCallTree() {
        return linkedCallTree;
    }

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
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
