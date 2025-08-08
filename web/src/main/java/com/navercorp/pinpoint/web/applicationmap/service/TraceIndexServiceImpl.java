/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.service;


import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TraceIndexServiceImpl implements TraceIndexService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationTraceIndexDao applicationTraceIndexDao;

    public TraceIndexServiceImpl(ApplicationTraceIndexDao applicationTraceIndexDao) {
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
    }

    @Override
    public LimitedScanResult<List<TransactionId>> getTraceIndex(String applicationName, Range range, int limit) {
        return getTraceIndex(applicationName, range, limit, true);
    }

    @Override
    public LimitedScanResult<List<TransactionId>> getTraceIndex(String applicationName, Range range, int limit, boolean backwardDirection) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");

        if (logger.isTraceEnabled()) {
            logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}", applicationName, range);
        }

        return this.applicationTraceIndexDao.scanTraceIndex(applicationName, range, limit, backwardDirection);
    }

}
