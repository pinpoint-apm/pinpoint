package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class AgentStatusQuery {
    private final List<SimpleAgentKey> agentKeyList;
    private final Instant queryTimestamp;

    private AgentStatusQuery(List<SimpleAgentKey> agentKeyList, Instant queryTimestamp) {
        this.agentKeyList = Objects.requireNonNull(agentKeyList, "agentStatusKeys");
        this.queryTimestamp = Objects.requireNonNull(queryTimestamp, "queryTimestamp");
    }

    public List<SimpleAgentKey> getAgentKeys() {
        return agentKeyList;
    }

    public long getQueryTimestamp() {
        return queryTimestamp.toEpochMilli();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private final List<SimpleAgentKey> agentKeyList = new ArrayList<>();

        private Builder() {
        }

        public void addAgentKey(String agentId, long agentStartTime) {
            Objects.requireNonNull(agentId, "agentId");
            SimpleAgentKey simpleAgentKey = new SimpleAgentKey(agentId, agentStartTime);

            this.addAgentKey(simpleAgentKey);
        }

        public void addAgentKey(SimpleAgentKey agentKey) {
            this.agentKeyList.add(agentKey);
        }

        public AgentStatusQuery build(Instant queryTimestamp) {
            return new AgentStatusQuery(new ArrayList<>(agentKeyList), queryTimestamp);
        }
    }

    public static AgentStatusQuery buildQuery(Collection<AgentInfo> agentInfos, Instant timestamp) {
        return buildQuery(agentInfos, AgentStatusQuery::apply, timestamp);
    }

    public static <T> AgentStatusQuery buildGenericQuery(Collection<T> agentInfos, Function<T, AgentInfo> agentInfoFunction, Instant timestamp) {
        return buildQuery(agentInfos, agentInfoFunction.andThen(AgentStatusQuery::apply), timestamp);
    }

    private static SimpleAgentKey apply(AgentInfo agentInfo) {
        if (agentInfo == null) {
            return null;
        }
        return new SimpleAgentKey(agentInfo.getAgentId().value(), agentInfo.getStartTimestamp());
    }

    public static <T> AgentStatusQuery buildQuery(Collection<T> agentInfos, Function<T, SimpleAgentKey> transform, Instant timestamp) {
        AgentStatusQuery.Builder builder = AgentStatusQuery.newBuilder();
        for (T agentInfo : agentInfos) {
            SimpleAgentKey apply = transform.apply(agentInfo);
            builder.addAgentKey(apply);
        }
        return builder.build(timestamp);
    }

    @Override
    public String toString() {
        return "AgentStatusQuery{" +
                "agentKeyList=" + agentKeyList +
                ", queryTimestamp=" + queryTimestamp +
                '}';
    }
}

