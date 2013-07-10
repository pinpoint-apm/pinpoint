package com.nhn.pinpoint.web.calltree.span;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 *
 */
public class SpanAlign {
	private int depth;
	private int sequence;
	private int parentSequence;
	private SpanBo spanBo;
	private SpanEventBo spanEventBo;
	private boolean span = true;
	private boolean hasChild = false;

	@Deprecated
	public SpanAlign(int depth, SpanBo spanBo) {
		this.depth = depth;
		this.spanBo = spanBo;
		this.span = true;
	}
	
	public SpanAlign(int depth, SpanBo spanBo, int sequence, int pSequence) {
		this.depth = depth;
		this.sequence = sequence;
		this.parentSequence = pSequence;
		this.spanBo = spanBo;
		this.span = true;
	}

	@Deprecated
	public SpanAlign(int depth, SpanBo spanBo, SpanEventBo spanEventBo) {
		this.depth = depth;
		this.spanBo = spanBo;
		this.spanEventBo = spanEventBo;
		this.span = false;
	}
	
	public SpanAlign(int depth, int sequence, int pSequence, SpanBo spanBo, SpanEventBo spanEventBo) {
		this.depth = depth;
		this.sequence = sequence;
		this.parentSequence = pSequence;
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

	public int getSequence() {
		return sequence;
	}

	public int getParentSequence() {
		return parentSequence;
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
        return "SpanAlign{" +
                "depth=" + depth +
                ", spanBo=" + spanBo +
                ", spanEventBo=" + spanEventBo +
                ", span=" + span +
                '}';
    }
}
