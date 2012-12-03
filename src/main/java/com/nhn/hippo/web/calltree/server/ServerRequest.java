package com.nhn.hippo.web.calltree.server;

/**
 * @author netspider
 */
public class ServerRequest {
	protected final String id;
	protected final Server from;
	protected final Server to;
	protected final Histogram histogram = new Histogram(100);

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

	public Histogram getHistogram() {
		return histogram;
	}

	public int getRequestCount() {
		return histogram.getSampleCount();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{from=").append(from).append(", to=").append(to).append(", histogram=").append(histogram).append("}");
		return sb.toString();
	}
}
