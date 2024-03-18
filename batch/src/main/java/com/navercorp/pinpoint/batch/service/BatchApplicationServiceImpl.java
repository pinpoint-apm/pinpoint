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

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDaoV2;
import com.navercorp.pinpoint.web.service.ApplicationService;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
@Service
public class BatchApplicationServiceImpl implements BatchApplicationService {

    private final ApplicationService applicationService;
    private final ApplicationTraceIndexDaoV2 applicationTraceIndexDao;

    public BatchApplicationServiceImpl(
            ApplicationService applicationService,
            ApplicationTraceIndexDaoV2 applicationTraceIndexDao
    ) {
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
    }

    @Override
    public List<ApplicationId> getAll() {
        return this.applicationService.getApplications()
                .stream()
                .map(Application::id)
                .toList();
    }

    @Override
    public boolean isActive(ApplicationId applicationId, Duration duration) {
        long now = System.currentTimeMillis();
        Range range = Range.between(now - duration.toMillis(), now);
        return hasTrace(applicationId, range);
    }

    private boolean hasTrace(ApplicationId applicationId, Range range) {
        return this.applicationTraceIndexDao.hasTraceIndex(applicationId, range,false);
    }

    @Override
    public void remove(ApplicationId applicationId) {
        this.applicationService.deleteApplication(applicationId);
    }

}
