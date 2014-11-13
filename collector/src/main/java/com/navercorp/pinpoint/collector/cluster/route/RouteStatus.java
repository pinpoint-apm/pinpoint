package com.nhn.pinpoint.collector.cluster.route;

public enum RouteStatus {

	OK(0, "OK"),

	BAD_REQUEST(400, "Bad Request"),

	NOT_FOUND(404, " Target Route Agent Not Found."),

	NOT_ACCEPTABLE(406, "Target Route Agent Not Acceptable Command."),
	
	AGENT_TIMEOUT(504, "Target Route Agent Timeout");

	private final int value;

	private final String reasonPhrase;

	private RouteStatus(int value, String reasonPhrase) {
		this.value = value;
		this.reasonPhrase = reasonPhrase;
	}

	public int getValue() {
		return value;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}
	
	@Override
	public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("{");
        sb.append("code=").append(getValue()).append(",");
        sb.append("message=").append(getReasonPhrase());
        sb.append('}');
        return sb.toString();
	}

}
