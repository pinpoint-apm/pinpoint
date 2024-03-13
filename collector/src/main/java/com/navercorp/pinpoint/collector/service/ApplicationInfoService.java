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

import com.navercorp.pinpoint.collector.dao.ApplicationInfoDao;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.util.UuidUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class ApplicationInfoService {

    private final ApplicationInfoDao applicationInfoDao;

    public ApplicationInfoService(ApplicationInfoDao applicationInfoDao) {
        this.applicationInfoDao = Objects.requireNonNull(applicationInfoDao, "applicationInfoDao");
    }

    @Cacheable(value = "applicationNameById", key = "#applicationId")
    public String getApplicationName(ApplicationId applicationId) {
        return this.applicationInfoDao.getApplicationName(applicationId);
    }

    @Cacheable(value = "applicationIdByName", key = "#applicationName")
    public ApplicationId getApplicationId(String applicationName) {
        ApplicationId applicationId = this.applicationInfoDao.getApplicationId(applicationName);
        if (applicationId != null) {
            return applicationId;
        }

        ApplicationId newApplicationId = ApplicationId.of(UuidUtils.createV4());
        return this.applicationInfoDao.putApplicationIdIfAbsent(applicationName, newApplicationId);
    }

    public void ensureApplicationIdInverseIndexed(String applicationName, ApplicationId applicationId) {
        this.applicationInfoDao.ensureInverse(applicationName, applicationId);
    }

}
