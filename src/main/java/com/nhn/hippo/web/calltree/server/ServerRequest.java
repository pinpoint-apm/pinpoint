package com.nhn.hippo.web.calltree.server;

import com.profiler.common.bo.HistogramBo;

/**
 * @author netspider
 */
public class ServerRequest {
	private final String id;
	private final Server from;
	private final Server to;
	private final HistogramBo histogram;

	public ServerRequest(Server from, Server to) {
		if (from == null) {
			throw new NullPointerException("from must not be null");
		}
		if (to == null) {
			throw new NullPointerException("to must not be null");
		}
		this.from = from;
		this.to = to;
		this.id = from.getId() + to.getId();
		this.histogram = new HistogramBo(100);
	}

	public ServerRequest(Server from, Server to, HistogramBo histogram) {
		if (from == null) {
			throw new NullPointerException("from must not be null");
		}
		if (to == null) {
			throw new NullPointerException("to must not be null");
		}
		if (histogram == null) {
			throw new NullPointerException("histogram must not be null");
		}
		this.from = from;
		this.to = to;
		this.id = from.getId() + to.getId();
		this.histogram = histogram;
	}

	public void addRequest(int elapsed) {
		histogram.addSample(elapsed);
	}

	public boolean isSelfCalled() {
		return from.getSequence() == to.getSequence();
	}

	public String getId() {
		return id;
	}

	public Server getFrom() {
		return from;
	}

	public Server getTo() {
		return to;
	}

	public HistogramBo getHistogram() {
		return histogram;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{from=").append(from).append(", to=").append(to).append(", histogram=").append(histogram).append("}");
		return sb.toString();
	}
}
