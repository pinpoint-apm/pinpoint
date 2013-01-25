package com.nhn.hippo.web.calltree.span;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;

/**
 *
 */
public class SpanAlign {
	private int depth;
	private SpanBo spanBo;
	private SubSpanBo subSpanBo;
	private boolean span = true;

	public SpanAlign(int depth, SpanBo spanBo) {
		this.depth = depth;
		this.spanBo = spanBo;
		this.span = true;
	}

	public SpanAlign(int depth, SpanBo spanBo, SubSpanBo subSpanBo) {
		this.depth = depth;
		this.spanBo = spanBo;
		this.subSpanBo = subSpanBo;
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

	public SubSpanBo getSubSpanBo() {
		return subSpanBo;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("SpanAlign={");
		sb.append("depth=").append(depth);

		if (subSpanBo != null) {
			sb.append(", orgDepth").append(subSpanBo.getDepth());
			sb.append(", subSpabBo=").append(subSpanBo.getServiceName());
		} else {
			sb.append(", spabBo=").append(spanBo.getServiceName());
		}

		sb.append("}\n");

		return sb.toString();
	}

}
