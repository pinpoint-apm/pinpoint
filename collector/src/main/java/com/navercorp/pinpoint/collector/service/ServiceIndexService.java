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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.ServiceIndexDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Service
public class ServiceIndexService {

    private final ServiceIndexRepository serviceIndexRepository;
    private final ServiceIndexDao serviceIndexDao;

    public ServiceIndexService(ServiceIndexRepository serviceIndexRepository, ServiceIndexDao serviceIndexDao) {
        this.serviceIndexRepository = Objects.requireNonNull(serviceIndexRepository, "serviceIndexRepository");
        this.serviceIndexDao = Objects.requireNonNull(serviceIndexDao, "serviceIndexDao");
    }

    public Long getServiceId(String serviceId) {
        Long serviceIdId = this.serviceIndexRepository.getServiceIdByName(serviceId);
        if (serviceIdId != null) {
            return serviceIdId;
        }
        return this.serviceIndexDao.selectServiceIdByName(serviceId, false);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public Long insertService(String serviceId) {
        Long id = this.serviceIndexDao.selectServiceIdByName(serviceId, true);
        if (id == null) {
            return this.serviceIndexDao.insertService(serviceId);
        } else {
            return id;
        }
    }

    public Long getApplicationId(Long serviceId, String applicationName) {
        Long applicationId = this.serviceIndexRepository.getApplicationId(serviceId, applicationName);
        if (applicationId != null) {
            return applicationId;
        }

        List<Long> applicationIds = this.serviceIndexDao.selectApplicationIdByServiceIdAndApplicationName(serviceId, applicationName, false);
        if (applicationIds.isEmpty()) {
            return null;
        } else if (applicationIds.size() == 1) {
            return applicationIds.get(0);
        } else {
            throw new IllegalStateException("too many applicationIds: " + applicationIds.size());
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public Long insertApplication(Long serviceId, String applicationName) {
        List<Long> applicationIds = this.serviceIndexDao.selectApplicationIdByServiceIdAndApplicationName(serviceId, applicationName, true);
        if (applicationIds.isEmpty()) {
            long applicationId = this.serviceIndexDao.insertApplication(applicationName);
            this.serviceIndexDao.insertServiceHasApplication(serviceId, applicationId);
            return applicationId;
        } else if (applicationIds.size() == 1) {
            return applicationIds.get(0);
        } else {
            throw new IllegalStateException("too many applicationIds: " + applicationIds.size());
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public void insertAgent(long applicationId, UUID agentId, String agentName) {
        this.serviceIndexDao.insertAgent(agentId, agentName);
        this.serviceIndexDao.insertApplicationHasAgent(applicationId, agentId);
    }

}
