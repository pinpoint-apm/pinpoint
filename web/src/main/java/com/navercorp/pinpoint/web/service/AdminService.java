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

import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Service;

import java.util.List;
import java.util.Map;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
public interface AdminService {

    int MIN_DURATION_DAYS_FOR_INACTIVITY = 30;
    String MIN_DURATION_DAYS_FOR_INACTIVITY_STR = "" + MIN_DURATION_DAYS_FOR_INACTIVITY;

    @Deprecated
    void removeApplicationName(String applicationName);

    void removeApplication(Service service, String applicationName, int serviceTypeCode);

    @Deprecated
    void removeAgentId(String applicationName, String agentId);

    void removeAgent(Service service, String applicationName, int serviceTypeCode, String agentId);

    @Deprecated
    void removeInactiveAgents(int durationDays);

    @Deprecated
    int removeInactiveAgentInApplication(String applicationName, int durationDays);

    @Deprecated
    Map<String, List<Application>> getAgentIdMap();

    @Deprecated
    Map<String, List<Application>> getDuplicateAgentIdMap();

    @Deprecated
    Map<String, List<Application>> getInactiveAgents(String applicationName, int durationDays);

}
