/*
 * Copyright 2024 NAVER Corp.
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

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.server.bo.ServiceInfo;
import com.navercorp.pinpoint.web.dao.ApplicationInfoDao;
import com.navercorp.pinpoint.web.dao.ServiceInfoDao;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class ServiceInfoServiceImpl implements ServiceInfoService {

    private final ServiceInfoDao serviceInfoDao;
    private final ApplicationInfoDao applicationInfoDao;


    public ServiceInfoServiceImpl(
            ServiceInfoDao serviceInfoDao,
            ApplicationInfoDao applicationInfoDao
    ) {
        this.serviceInfoDao = Objects.requireNonNull(serviceInfoDao, "serviceInfoDao");
        this.applicationInfoDao = Objects.requireNonNull(applicationInfoDao, "applicationInfoDao");
    }

    @Override
    @Cacheable(value = "serviceInfoById", key = "#id")
    public ServiceInfo getServiceInfo(ServiceId id) {
        return this.serviceInfoDao.getServiceInfo(id);
    }

    @Override
    @Cacheable(value = "serviceIdByName", key = "#serviceName")
    public ServiceId getServiceId(String serviceName) {
        return this.serviceInfoDao.getServiceId(serviceName);
    }

    @Override
    public List<ApplicationId> getApplicationIds(ServiceId serviceId) {
        return this.applicationInfoDao.getApplications(serviceId);
    }

}
