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

package com.navercorp.pinpoint.collector.dao.mysql;

import com.navercorp.pinpoint.collector.dao.ServiceIndexDao;
import com.navercorp.pinpoint.collector.vo.ApplicationIndex;
import com.navercorp.pinpoint.collector.vo.ServiceHasApplication;
import com.navercorp.pinpoint.collector.vo.ServiceIndex;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Service
@ConditionalOnProperty(name = "pinpoint.experimental.service-index", havingValue = "empty", matchIfMissing = true)
public class EmptyServiceIndexDao implements ServiceIndexDao {

    @Override
    public List<ServiceIndex> selectAllServices() {
        return List.of();
    }

    @Override
    public List<ApplicationIndex> selectAllApplications() {
        return List.of();
    }

    @Override
    public List<ServiceHasApplication> selectAllServiceHasApplications() {
        return List.of();
    }

    @Override
    public Long selectServiceIdByName(String serviceId, boolean writeLock) {
        return 0L;
    }

    @Override
    public List<Long> selectApplicationIdByServiceIdAndApplicationName(Long serviceId, String applicationName, boolean writeLock) {
        return List.of();
    }

    @Override
    public Long insertService(String serviceId) {
        return 0L;
    }

    @Override
    public Long insertApplication(String applicationName) {
        return 0L;
    }

    @Override
    public void insertAgent(UUID agentId, String agentName) {
    }

    @Override
    public void insertServiceHasApplication(Long serviceId, Long applicationId) {
    }

    @Override
    public void insertApplicationHasAgent(Long applicationId, UUID agentId) {
    }

}
