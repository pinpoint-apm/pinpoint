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

import com.navercorp.pinpoint.web.dao.ApplicationInfoDao;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Component
public class ApplicationInfoService {

    private final ApplicationInfoDao applicationInfoDao;

    public ApplicationInfoService(ApplicationInfoDao applicationInfoDao) {
        this.applicationInfoDao = Objects.requireNonNull(applicationInfoDao, "applicationInfoDao");
    }

    @Cacheable(value = "applicationNameById", key = "#applicationId")
    public String getApplicationName(UUID applicationId) {
        return this.applicationInfoDao.getApplicationName(applicationId);
    }

    @Cacheable(value = "applicationIdByName", key = "#applicationName")
    public UUID getApplicationId(String applicationName) {
        return this.applicationInfoDao.getApplicationId(applicationName);
    }

}
