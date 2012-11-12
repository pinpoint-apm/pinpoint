package com.nhn.hippo.web.calltree.application;

/**
 * @author netspider
 */
public class ApplicationRequest {
    private final String id;
    private final Application from;
    private final Application to;
    private int callCount = 1;

    public ApplicationRequest(Application from, Application to) {
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

    public boolean isSelfCalled() {
        return from.getSequence() == to.getSequence();
    }

    public void increaseCallCount() {
        callCount++;
    }

    public String getId() {
        return id;
    }

    public Application getFrom() {
        return from;
    }

    public Application getTo() {
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
