package com.nhn.hippo.web.calltree.server;

/**
 * @author netspider
 */
public class TerminalServerRequest extends ServerRequest {

	private final int requestCount;

	public TerminalServerRequest(Server from, Server to, int requestCount) {
		super(from, to);
		this.requestCount = requestCount;
	}

	public int getRequestCount() {
		return requestCount;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{from=").append(from).append(", to=").append(to).append(", histogram=").append(histogram).append("}");
		return sb.toString();
	}
}
