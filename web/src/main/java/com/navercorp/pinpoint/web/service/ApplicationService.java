/*
 * Copyright 2017 NAVER Corp.
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Taejin Koo
 */
@Service
public interface ApplicationService {

    boolean isExistApplicationName(String applicationName);

    List<Application> getApplications();

    List<String> getAgents(UUID applicationId);

    void deleteApplication(UUID applicationId);

    void deleteAgents(Map<UUID, List<String>> applicationAgentIdMap);

    void deleteAgent(UUID applicationId, String agentId);

}
