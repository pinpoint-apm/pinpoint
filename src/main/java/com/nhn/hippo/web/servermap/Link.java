package com.nhn.hippo.web.servermap;

/**
 * 
 * @author netspider
 * 
 */
public class Link {
	protected final String id;
	protected final Node from;
	protected final Node to;
	protected final Histogram histogram = new Histogram(100);

	public Link(Node from, Node to) {
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
	
	public Link(Node from, Node to, long callCount) {
		this(from, to);
		histogram.addUnknownSample(callCount);
	}

	public void addElapsedTime(int elapsed) {
		histogram.addSample(elapsed);
	}

	public boolean isLocalCall() {
		return from.getSequence() == to.getSequence();
	}

	public String getId() {
		return id;
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
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
		sb.append("{ from=").append(from).append(", to=").append(to).append(", histogram=").append(histogram).append(" }");
		return sb.toString();
	}
}
