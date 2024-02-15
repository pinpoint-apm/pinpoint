/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.collector.vo.ApplicationIndex;
import com.navercorp.pinpoint.collector.vo.ServiceHasApplication;
import com.navercorp.pinpoint.collector.vo.ServiceIndex;

import java.util.List;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
public interface ServiceIndexDao {

    List<ServiceIndex> selectAllServices();
    List<ApplicationIndex> selectAllApplications();
    List<ServiceHasApplication> selectAllServiceHasApplications();
    Long selectServiceIdByName(String serviceId, boolean writeLock);
    List<Long> selectApplicationIdByServiceIdAndApplicationName(Long serviceId, String applicationName, boolean writeLock);
    Long insertService(String serviceId);
    Long insertApplication(String applicationName);
    void insertAgent(UUID agentId, String agentName);
    void insertServiceHasApplication(Long serviceId, Long applicationId);
    void insertApplicationHasAgent(Long applicationId, UUID agentId);

}
