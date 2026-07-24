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


import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.scatter.vo.DotMetaData;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TraceIndexServiceImpl implements TraceIndexService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TraceIndexDao traceIndexDao;

    public TraceIndexServiceImpl(TraceIndexDao traceIndexDao) {
        this.traceIndexDao = Objects.requireNonNull(traceIndexDao, "traceIndexDao");
    }

    @Override
    public LimitedScanResult<List<ServerTraceId>> getTraceIndexV2(int serviceUid, String applicationName, int serviceTypeCode, Range range, int limit) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");

        if (logger.isTraceEnabled()) {
            logger.trace("scan(selectTraceIdsFromTraceIndex) {}, {}", applicationName, range);
        }

        LimitedScanResult<List<DotMetaData>> listLimitedScanResult = this.traceIndexDao.scanTraceIndex(serviceUid, applicationName, serviceTypeCode, range, limit);
        List<ServerTraceId> transactionIds = listLimitedScanResult.scanData().stream()
                .map(meta -> meta.getDot().getTransactionId())
                .toList();
        return new LimitedScanResult<>(listLimitedScanResult.limitedTime(), transactionIds);
    }

}
