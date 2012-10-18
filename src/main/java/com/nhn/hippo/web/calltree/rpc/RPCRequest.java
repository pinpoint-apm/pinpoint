package com.nhn.hippo.web.calltree.rpc;

/**
 * @author netspider
 */
public class RPCRequest {
    private final String id;
    private final RPC from;
    private final RPC to;
    private int callCount = 1;

    public RPCRequest(RPC from, RPC to) {
        if (from == null) {
            throw new NullPointerException("form must not be null");
        }
        if (to == null) {
            throw new NullPointerException("to must not be null");
        }
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
