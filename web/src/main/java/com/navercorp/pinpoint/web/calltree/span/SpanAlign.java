/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;

/**
 * @author emeroad
 */
public class SpanAlign {
	private int depth;
	private SpanBo spanBo;
	private SpanEventBo spanEventBo;
	private boolean span = true;
	private boolean hasChild = false;

	public SpanAlign(int depth, SpanBo spanBo) {
        if (spanBo == null) {
            throw new NullPointerException("spanBo must not be null");
        }
        this.depth = depth;
		this.spanBo = spanBo;
		this.span = true;
	}
	
	public SpanAlign(int depth, SpanBo spanBo, SpanEventBo spanEventBo) {
        if (spanBo == null) {
            throw new NullPointerException("spanBo must not be null");
        }
        if (spanEventBo == null) {
            throw new NullPointerException("spanEventBo must not be null");
        }
        this.depth = depth;
		this.spanBo = spanBo;
		this.spanEventBo = spanEventBo;
		this.span = false;
	}

	public void setSpan(boolean span) {
		this.span = span;
	}

	public boolean isSpan() {
		return span;
	}

	public int getDepth() {
		return depth;
	}

	public SpanBo getSpanBo() {
		return spanBo;
	}

	public SpanEventBo getSpanEventBo() {
		return spanEventBo;
	}

    public boolean isHasChild() {
		return hasChild;
	}

	public void setHasChild(boolean hasChild) {
		this.hasChild = hasChild;
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpanAlign{");
        sb.append("depth=").append(depth);
        if (span) {
            sb.append(", spanBo=").append(spanBo);
            sb.append(", spanEventBo=").append(spanEventBo);
        } else {
            sb.append(", spanEventBo=").append(spanEventBo);
            sb.append(", spanBo=").append(spanBo);
        }
        sb.append(", span=").append(span);
        sb.append(", hasChild=").append(hasChild);
        sb.append('}');
        return sb.toString();
    }
}
