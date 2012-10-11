package com.nhn.hippo.web.calltree.server;

/**
 * @author netspider
 */
public class ServerRequest {
    private final String id;
    private final Server from;
    private final Server to;
    private int callCount = 1;

    public ServerRequest(Server from, Server to) {
        this.from = from;
        this.to = to;
        this.id = from.getId() + to.getId();
    }

    public boolean isSelfCalled() {
        return from.getSequence() == to.getSequence();
    }

    public void increaseCallCount() {
        callCount++;
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
