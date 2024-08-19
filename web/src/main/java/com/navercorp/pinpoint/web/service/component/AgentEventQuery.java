package com.navercorp.pinpoint.web.service.component;

import com.navercorp.pinpoint.common.server.util.AgentEventType;

import java.util.Objects;
import java.util.Set;

public class AgentEventQuery {
    public enum QueryType {
        INCLUDE,
        EXCLUDE,
        ALL
    }

    public static final AgentEventQuery ALL = new AgentEventQuery(QueryType.ALL, Set.of());

    private final QueryType queryType;
    private final Set<AgentEventType> eventTypes;
    private final boolean oneRowScan;

    public AgentEventQuery(QueryType queryType, Set<AgentEventType> eventTypes) {
        this.queryType = Objects.requireNonNull(queryType, "queryType");
        this.eventTypes = Objects.requireNonNull(eventTypes, "eventTypes");
        this.oneRowScan = false;
    }

    public AgentEventQuery(QueryType queryType, Set<AgentEventType> eventTypes, boolean oneRowScan) {
        this.queryType = Objects.requireNonNull(queryType, "queryType");
        this.eventTypes = Objects.requireNonNull(eventTypes, "eventTypes");
        this.oneRowScan = oneRowScan;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public boolean isOneRowScan() {
        return oneRowScan;
    }

    public AgentEventQuery withOneRowScan() {
        return new AgentEventQuery(this.queryType, this.eventTypes, true);
    }

    public Set<AgentEventType> getEventTypes() {
        return eventTypes;
    }

    public static AgentEventQuery include(Set<AgentEventType> includeEventTypes) {
        return new AgentEventQuery(QueryType.INCLUDE, includeEventTypes);
    }

    public static AgentEventQuery exclude(Set<AgentEventType> excludeEventTypes) {
        return new AgentEventQuery(QueryType.EXCLUDE, excludeEventTypes);
    }

    public static AgentEventQuery all() {
        return ALL;
    }

    @Override
    public String toString() {
        return "AgentEventQuery{" +
                "queryType=" + queryType +
                ", eventTypes=" + eventTypes +
                ", oneRowScan=" + oneRowScan +
                '}';
    }
}
