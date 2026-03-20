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

import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentAndStatus;
import com.navercorp.pinpoint.web.vo.timeline.inspector.InspectorTimeline;

import java.util.List;
import java.util.Optional;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
public interface AgentInfoService {

    List<DetailedAgentAndStatus> getAgentsStatisticsList(Range range);

    List<AgentInfo> getAgentInfoByApplicationName(String applicationName, long timestamp);

    AgentAndStatus findAgentInfoAndStatus(String agentId, long timestamp);

    DetailedAgentAndStatus findDetailedAgentInfoAndStatus(String agentId, long timestamp);

    AgentInfo findAgentInfo(String agentId, long timestamp);

    AgentInfo findAgentInfo(String agentId, long fromTimestamp, long toTimestamp);

    AgentInfo getAgentInfo(String agentId, long agentStartTime);

    List<AgentInfo> getAgentInfos(List<SimpleAgentKey> simpleAgentKeyList);

    AgentStatus findAgentStatus(String agentId, long timestamp);

    List<Optional<AgentStatus>> getAgentStatus(AgentStatusQuery query);

    InspectorTimeline getAgentStatusTimeline(String applicationName, String agentId, Range range, int... excludeAgentEventTypeCodes);

}
