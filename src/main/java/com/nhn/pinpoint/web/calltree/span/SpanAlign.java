package com.nhn.pinpoint.web.calltree.span;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 *
 */
public class SpanAlign {
	private int depth;
	private SpanBo spanBo;
	private SpanEventBo spanEventBo;
	private boolean span = true;

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

    @Override
    public String toString() {
        return "SpanAlign{" +
                "depth=" + depth +
                ", spanBo=" + spanBo +
                ", spanEventBo=" + spanEventBo +
                ", span=" + span +
                '}';
    }
}
