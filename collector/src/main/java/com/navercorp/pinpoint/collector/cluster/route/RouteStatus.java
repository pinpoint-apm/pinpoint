package com.nhn.pinpoint.collector.cluster.route;

/**
 * @author koo.taejin <kr14910>
 */
public enum RouteStatus {

	OK(0, "OK"),

	BAD_REQUEST(400, "Bad Request"),

	NOT_FOUND(404, " Target Route Agent Not Found."),

	NOT_ACCEPTABLE(406, "Target Route Agent Not Acceptable."),
	
	NOT_ACCEPTABLE_UNKNOWN(450, "Target Route Agent Not Acceptable."),
    NOT_ACCEPTABLE_COMMAND(451, "Target Route Agent Not Acceptable command."),
    NOT_ACCEPTABLE_AGENT_TYPE(452, "Target Route Agent Not Acceptable agent type.."),
	
	AGENT_TIMEOUT(504, "Target Route Agent Timeout"),
	
	CLOSED(606, "Target Route Agent Closed.");

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
