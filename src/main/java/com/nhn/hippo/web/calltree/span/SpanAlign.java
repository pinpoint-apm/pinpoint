package com.nhn.hippo.web.calltree.span;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEvent;

/**
 *
 */
public class SpanAlign {
	private int depth;
	private SpanBo spanBo;
	private SpanEvent spanEventBo;
	private boolean span = true;

	public SpanAlign(int depth, SpanBo spanBo) {
		this.depth = depth;
		this.spanBo = spanBo;
		this.span = true;
	}

	public SpanAlign(int depth, SpanBo spanBo, SpanEvent spanEventBo) {
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

	public SpanEvent getSpanEventBo() {
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
