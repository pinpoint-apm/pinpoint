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

package com.navercorp.pinpoint.batch.service;

import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class BatchApplicationIndexV1ServiceImpl implements BatchApplicationIndexService {

    private final ApplicationIndexDao applicationIndexDao;

    public BatchApplicationIndexV1ServiceImpl(ApplicationIndexDao applicationIndexDao) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
    }

    @Override
    public List<Application> selectAllApplications() {
        return this.applicationIndexDao.selectAllApplicationNames();
    }

    @Override
    @Deprecated
    public List<String> selectAllApplicationNames() {
        return this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getApplicationName)
                .toList();
    }

    @Override
    public List<String> selectAgentIds(String applicationName) {
        return this.applicationIndexDao.selectAgentIds(applicationName);
    }

    @Override
    public List<String> selectAgentIds(String applicationName, int serviceTypeCode) {
        return this.applicationIndexDao.selectAgentIds(applicationName, serviceTypeCode);
    }
}
