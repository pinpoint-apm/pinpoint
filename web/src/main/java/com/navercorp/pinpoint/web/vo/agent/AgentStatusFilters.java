package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;

public class AgentStatusFilters {

    private static final AgentStatusFilter ACCEPT_ALL = new AcceptAll();
    private static final AgentStatusFilter RUNNING = new RunningFilter();

    public static AgentStatusFilter acceptAll() {
        return ACCEPT_ALL;
    }

    public static AgentStatusFilter running() {
        return RUNNING;
    }

    public static AgentStatusFilter recentRunning(long minEventTimestamp) {
        return new RecentRunningFilter(minEventTimestamp);
    }

    private static class AcceptAll implements AgentStatusFilter {
        @Override
        public boolean test(AgentStatus agentStatus) {
            return true;
        }
    }

    private static class RunningFilter implements AgentStatusFilter {
        @Override
        public boolean test(AgentStatus agentStatus) {
            if (agentStatus == null) {
                return false;
            }
            return agentStatus.getState() == AgentLifeCycleState.RUNNING;
        }
    }

    private record RecentRunningFilter(long minEventTimestamp) implements AgentStatusFilter {
        @Override
        public boolean test(AgentStatus agentStatus) {
            if (agentStatus == null) {
                return false;
            }
            return
                    agentStatus.getState() == AgentLifeCycleState.RUNNING ||
                    agentStatus.getEventTimestamp() >= minEventTimestamp;
        }
    }

}
