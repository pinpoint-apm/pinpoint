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
import com.navercorp.pinpoint.common.server.bo.ApplicationInfo;
import com.navercorp.pinpoint.common.server.bo.ApplicationSelector;
import com.navercorp.pinpoint.common.util.UuidUtils;
import com.navercorp.pinpoint.web.dao.ApplicationInfoDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Component
public class ApplicationInfoService {

    private final ApplicationInfoDao applicationInfoDao;

    public ApplicationInfoService(ApplicationInfoDao applicationInfoDao) {
        this.applicationInfoDao = Objects.requireNonNull(applicationInfoDao, "applicationInfoDao");
    }

    @Cacheable(value = "applicationById", key = "#applicationId")
    public Application getApplication(ApplicationId applicationId) {
        return this.applicationInfoDao.getApplication(applicationId);
    }

    @Cacheable(value = "applicationIdBySelector", key = "#application")
    public ApplicationId getApplicationId(ApplicationSelector application) {
        ApplicationId applicationId = this.applicationInfoDao.getApplicationId(application);
        if (applicationId != null) {
            return applicationId;
        }

        ApplicationId newApplicationId = ApplicationId.of(UuidUtils.createV4());
        ApplicationInfo newApplication = new ApplicationInfo(newApplicationId, application.serviceId(),
                application.name(), application.serviceTypeCode());
        return this.applicationInfoDao.putApplicationIdIfAbsent(newApplication);
    }

}
