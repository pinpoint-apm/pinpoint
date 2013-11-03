package com.nhn.pinpoint.web.calltree.server;

import com.nhn.pinpoint.web.vo.ResponseHistogram;

/**
 * @author netspider
 */
@Deprecated
public class ServerRequest {
	protected final String id;
	protected final Server from;
	protected final Server to;
	private final ResponseHistogram histogram;

	public ServerRequest(Server from, Server to, ResponseHistogram histogram) {
		if (from == null) {
			throw new NullPointerException("from must not be null");
		}
		if (to == null) {
			throw new NullPointerException("to must not be null");
		}
		this.from = from;
		this.to = to;
		this.id = from.getId() + to.getId();
		this.histogram = histogram;
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

	public ResponseHistogram getHistogram() {
		return histogram;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{from=").append(from).append(", to=").append(to).append(", histogram=").append(histogram).append("}");
		return sb.toString();
	}
}
