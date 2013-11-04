package com.nhn.pinpoint.web.calltree.span;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

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
		this.depth = depth;
		this.spanBo = spanBo;
		this.span = true;
	}
	
	public SpanAlign(int depth, SpanBo spanBo, SpanEventBo spanEventBo) {
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
