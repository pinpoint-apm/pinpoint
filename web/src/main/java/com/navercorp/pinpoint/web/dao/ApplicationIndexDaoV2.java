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

package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationIndexDaoV2 {

    List<Application> selectAllApplications();

    List<Application> selectApplicationName(ApplicationId applicationId);

    List<String> selectAgentIds(ApplicationId applicationId);

    void deleteApplication(ApplicationId applicationId);

    void deleteAgentIds(Map<ApplicationId, List<String>> applicationAgentIdMap);

    void deleteAgentId(ApplicationId applicationId, String agentId);

}
