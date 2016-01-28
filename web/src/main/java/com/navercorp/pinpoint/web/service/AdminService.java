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

import java.util.List;
import java.util.Map;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
public interface AdminService {

    void removeApplicationName(String applicationName);

    void removeAgentId(String applicationName, String agentId);

    void removeInactiveAgents(int durationDays);

    Map<String, List<Application>> getAgentIdMap();

    Map<String, List<Application>> getDuplicateAgentIdMap();

    Map<String, List<Application>> getInactiveAgents(String applicationName, int durationDays);

}
