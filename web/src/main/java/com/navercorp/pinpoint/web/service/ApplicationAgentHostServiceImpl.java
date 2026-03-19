/*
 * Copyright 2026 NAVER Corp.
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

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.tree.ApplicationAgentHostList;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.navercorp.pinpoint.web.service.ApplicationAgentListService.ACTUAL_AGENT_INFO_PREDICATE;

@Service
public class ApplicationAgentHostServiceImpl implements ApplicationAgentHostService {

    private final ApplicationAgentListService applicationAgentListService;

    public ApplicationAgentHostServiceImpl(ApplicationAgentListService applicationAgentListService) {
        this.applicationAgentListService = Objects.requireNonNull(applicationAgentListService, "applicationAgentListService");
    }

    @Override
    public ApplicationAgentHostList getApplicationAgentHostList(int offset, int limit, int durationHours, List<Application> applicationList, Predicate<AgentInfo> agentInfoFilter) {
        if (offset > applicationList.size()) {
            ApplicationAgentHostList.Builder builder = newBuilder(offset, offset, applicationList.size());
            return builder.build();
        }
        final long currentTime = System.currentTimeMillis();
        final int startIndex = offset - 1;
        final int endIndex = Math.min(startIndex + limit, applicationList.size());

        ApplicationAgentHostList.Builder builder = newBuilder(offset, endIndex, applicationList.size());
        for (int i = startIndex; i < endIndex; i++) {
            Application application = applicationList.get(i);

            List<AgentInfo> agentInfoList = getAgentInfoList(application, currentTime, durationHours, agentInfoFilter);
            builder.addAgentInfo(application.getApplicationName(), agentInfoList);
        }
        return builder.build();
    }

    private List<AgentInfo> getAgentInfoList(Application application, long timestamp, int durationHours, Predicate<AgentInfo> agentInfoFilter) {
        if (durationHours <= 0) {
            return applicationAgentListService.allAgentList(application.getApplicationName(), application.getServiceType(), timestamp, agentInfoFilter.and(ACTUAL_AGENT_INFO_PREDICATE)).stream()
                    .map(AgentAndStatus::getAgentInfo)
                    .collect(Collectors.toList());
        } else {
            Range range = Range.between(timestamp - TimeUnit.HOURS.toMillis(durationHours), timestamp);
            TimeWindow timeWindow = new TimeWindow(range);
            return applicationAgentListService.activeStatusAgentList(application.getApplicationName(), application.getServiceType(), timeWindow, agentInfoFilter).stream()
                    .map(AgentAndStatus::getAgentInfo)
                    .collect(Collectors.toList());
        }
    }

    private ApplicationAgentHostList.Builder newBuilder(int offset, int endIndex, int totalApplications) {
        return ApplicationAgentHostList.newBuilder(offset, endIndex, totalApplications);
    }
}
