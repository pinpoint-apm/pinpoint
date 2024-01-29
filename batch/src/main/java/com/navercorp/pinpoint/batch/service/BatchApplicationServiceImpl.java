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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
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

    private final ApplicationIndexDao applicationIndexDao;
    private final ApplicationTraceIndexDao applicationTraceIndexDao;

    public BatchApplicationServiceImpl(
            ApplicationIndexDao applicationIndexDao,
            ApplicationTraceIndexDao applicationTraceIndexDao
    ) {
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
    }

    @Override
    public List<String> getAll() {
        return this.applicationIndexDao.selectAllApplicationNames()
                .stream()
                .map(Application::getName)
                .toList();
    }

    @Override
    public boolean isActive(String applicationName, Duration duration) {
        long now = System.currentTimeMillis();
        Range range = Range.between(now - duration.toMillis(), now);
        return hasTrace(applicationName, range);
    }

    private boolean hasTrace(String applicationName, Range range) {
        return this.applicationTraceIndexDao.hasTraceIndex(applicationName, range,false);
    }

    @Override
    public void remove(String applicationName) {
        this.applicationIndexDao.deleteApplicationName(applicationName);
    }
}
