package com.navercorp.pinpoint.web.vo.tree;

import com.navercorp.pinpoint.web.vo.agent.AgentInfo;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public class SortBy<T> {
    public static Comparator<AgentInfo> AGENT_NAME_ASC = Comparator.comparing(AgentInfo::getAgentName)
            .thenComparing(AgentInfo::getAgentId);

    public static Comparator<AgentInfo> AGENT_NAME_DESC = AGENT_NAME_ASC.reversed();

    public static Comparator<AgentInfo> AGENT_ID_ASC = Comparator.comparing(AgentInfo::getAgentId)
            .thenComparing(AgentInfo::getAgentName);

    public static Comparator<AgentInfo> AGENT_ID_DESC = AGENT_ID_ASC.reversed();

    public static Comparator<AgentInfo> LAST_STARTED_TIME = Comparator.comparingLong(AgentInfo::getStartTimestamp)
                        .reversed()
                        .thenComparing(AgentInfo::getAgentId);

    public static <T> SortBy<T> agentNameAsc(Function<T, AgentInfo> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor");
        return new SortBy<>(Comparator.comparing(keyExtractor, AGENT_NAME_ASC));
    }

    public static <T> SortBy<T> agentIdAsc(Function<T, AgentInfo> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor");
        return new SortBy<>(Comparator.comparing(keyExtractor, AGENT_ID_ASC));
    }

    private final Comparator<T> comparator;

    private SortBy(Comparator<T> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "comparator");
    }

    public Comparator<T> getComparator() {
        return comparator;
    }
}
