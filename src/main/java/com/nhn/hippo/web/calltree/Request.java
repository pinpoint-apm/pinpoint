package com.nhn.hippo.web.calltree;

/**
 * 
 * @author netspider
 * 
 */
public class Request {
	private final String id;
	private final RPC from;
	private final RPC to;
	private int callCount = 1;

	public Request(RPC from, RPC to) {
		this.from = from;
		this.to = to;
		this.id = from.getId() + to.getId();
	}

	public void increaseCallCount() {
		callCount++;
	}

	public String getId() {
		return id;
	}

	public RPC getFrom() {
		return from;
	}

	public RPC getTo() {
		return to;
	}

	public int getCallCount() {
		return callCount;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{from=").append(from).append(", to=").append(to).append(", cc=").append(callCount).append("}");
		return sb.toString();
	}
}
