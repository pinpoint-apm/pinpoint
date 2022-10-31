package com.navercorp.pinpoint.web.vo.tree;

import com.navercorp.pinpoint.web.vo.agent.AgentInfo;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public class SortByAgentInfo<T> {

    public enum Rules {
        AGENT_NAME_ASC(Comparator.comparing(AgentInfo::getAgentName)
                .thenComparing(AgentInfo::getAgentId)),

        AGENT_NAME_DESC(AGENT_NAME_ASC.getRule().reversed()),

        AGENT_ID_ASC(Comparator.comparing(AgentInfo::getAgentId)
                .thenComparing(AgentInfo::getAgentName)),

        AGENT_ID_DESC(AGENT_ID_ASC.getRule().reversed()),

        LAST_STARTED_TIME(Comparator.comparingLong(AgentInfo::getStartTimestamp)
                .reversed()
                .thenComparing(AgentInfo::getAgentId));

        private final Comparator<AgentInfo> rule;

        Rules(Comparator<AgentInfo> rule) {
            this.rule = rule;
        }

        public Comparator<AgentInfo> getRule() {
            return rule;
        }
    }

    public static <T> SortByAgentInfo<T> agentNameAsc(Function<T, AgentInfo> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor");
        return comparing(keyExtractor, Rules.AGENT_NAME_ASC.getRule());
    }

    public static <T> SortByAgentInfo<T> agentIdAsc(Function<T, AgentInfo> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor");
        return comparing(keyExtractor, Rules.AGENT_ID_ASC.getRule());
    }

    public static <T> SortByAgentInfo<T> comparing(Function<T, AgentInfo> keyExtractor, Comparator<AgentInfo> comparator) {
        Objects.requireNonNull(keyExtractor, "keyExtractor");
        Objects.requireNonNull(comparator, "comparator");
        return new SortByAgentInfo<>(Comparator.comparing(keyExtractor, comparator));
    }

    public static Rules of(String sortBy) {
        return Rules.valueOf(sortBy);
    }

    private final Comparator<T> comparator;

    private SortByAgentInfo(Comparator<T> comparator) {
        this.comparator = Objects.requireNonNull(comparator, "comparator");
    }

    public Comparator<T> getComparator() {
        return comparator;
    }
}
