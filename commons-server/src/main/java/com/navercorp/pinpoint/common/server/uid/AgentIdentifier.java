package com.navercorp.pinpoint.common.server.uid;

import java.util.Objects;

public class AgentIdentifier {

    private final String id;
    private final String name;
    private final long startTimestamp;

    public AgentIdentifier(String id, String name, long startTimestamp) {
        this.id = Objects.requireNonNull(id, "agentId");
        this.name = Objects.requireNonNull(name, "agentName");
        this.startTimestamp = startTimestamp;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentIdentifier that = (AgentIdentifier) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "AgentIdentifier{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", startTimestamp=" + startTimestamp +
                '}';
    }
}
