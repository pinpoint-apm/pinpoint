package com.navercorp.pinpoint.web.vo.agent;

import com.navercorp.pinpoint.common.server.bo.AgentStatus;
import java.util.function.Predicate;

public interface AgentStatusFilter extends Predicate<AgentStatus> {
}
