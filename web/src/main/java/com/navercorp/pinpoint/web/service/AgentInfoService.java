/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.AgentStatusQuery;
import com.navercorp.pinpoint.web.vo.ApplicationAgentHostList;
import com.navercorp.pinpoint.web.vo.ApplicationAgentsList;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.timeline.inspector.InspectorTimeline;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
public interface AgentInfoService {

     int NO_DURATION = -1;

    ApplicationAgentsList getAllApplicationAgentsList(AgentInfoFilter filter, long timestamp);

    ApplicationAgentsList getApplicationAgentsList(ApplicationAgentsList.GroupBy key, AgentInfoFilter filter, String applicationName, long timestamp);

    ApplicationAgentHostList getApplicationAgentHostList(int offset, int limit, int durationDays);

    Set<AgentAndStatus> getAgentsByApplicationName(String applicationName, long timestamp);

    Set<AgentInfo> getAgentsByApplicationNameWithoutStatus(String applicationName, long timestamp);

    Set<AgentAndStatus> getRecentAgentsByApplicationName(String applicationName, long timestamp, long timeDiff);

    AgentAndStatus getAgentInfo(String agentId, long timestamp);

    AgentInfo getAgentInfoWithoutStatus(String agentId, long timestamp);

    AgentInfo getAgentInfoWithoutStatus(String agentId, long agentStartTime, int deltaTimeInMilliseconds);

    AgentStatus getAgentStatus(String agentId, long timestamp);

    List<Optional<AgentStatus>> getAgentStatus(AgentStatusQuery query);

    boolean isActiveAgent(String agentId, Range range);

    InspectorTimeline getAgentStatusTimeline(String agentId, Range range, int... excludeAgentEventTypeCodes);

    boolean isExistAgentId(String agentId);

}
