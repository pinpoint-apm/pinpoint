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

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDaoV2;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
@Component
public class ApplicationTraceIndexServiceImpl implements ApplicationTraceIndexService {

    private final ApplicationTraceIndexDao applicationTraceIndexDao;
    private final ApplicationTraceIndexDaoV2 applicationTraceIndexDaoV2;
    private final ApplicationInfoService applicationInfoService;

    public ApplicationTraceIndexServiceImpl(
            ApplicationTraceIndexDao applicationTraceIndexDao,
            ApplicationTraceIndexDaoV2 applicationTraceIndexDaoV2,
            ApplicationInfoService applicationInfoService
    ) {
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.applicationTraceIndexDaoV2 = Objects.requireNonNull(applicationTraceIndexDaoV2, "applicationTraceIndexDaoV2");
        this.applicationInfoService = Objects.requireNonNull(applicationInfoService, "applicationInfoService");
    }

    @Override
    public boolean hasTraceIndex(String applicationName, Range range, boolean backwardDirection) {
        UUID applicationId = getApplicationId(applicationName);
        return applicationTraceIndexDao.hasTraceIndex(applicationName, range, backwardDirection) || applicationTraceIndexDaoV2.hasTraceIndex(applicationId, range, backwardDirection);
    }

    @Override
    public LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, Range range, int limit, boolean backwardDirection) {
        UUID applicationId = getApplicationId(applicationName);
        LimitedScanResult<List<TransactionId>> r1 = applicationTraceIndexDao.scanTraceIndex(applicationName, range, limit, backwardDirection);
        LimitedScanResult<List<TransactionId>> r2 = applicationTraceIndexDaoV2.scanTraceIndex(applicationId, range, limit, backwardDirection);
        return merge(r1, r2);
    }

    @Override
    public LimitedScanResult<List<Dot>> scanTraceScatterData(String applicationName, Range range, int limit, boolean scanBackward) {
        UUID applicationId = getApplicationId(applicationName);
        LimitedScanResult<List<Dot>> r1 = applicationTraceIndexDao.scanTraceScatterData(applicationName, range, limit, scanBackward);
        LimitedScanResult<List<Dot>> r2 = applicationTraceIndexDaoV2.scanTraceScatterData(applicationId, range, limit, scanBackward);
        return merge(r1, r2);
    }

    @Override
    public LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, DragArea dragArea, int limit) {
        UUID applicationId = getApplicationId(applicationName);
        LimitedScanResult<List<TransactionId>> r1 = applicationTraceIndexDao.scanTraceIndex(applicationName, dragArea, limit);
        LimitedScanResult<List<TransactionId>> r2 = applicationTraceIndexDaoV2.scanTraceIndex(applicationId, dragArea, limit);
        return merge(r1, r2);
    }

    @Override
    public LimitedScanResult<List<Dot>> scanScatterData(String applicationName, DragAreaQuery dragAreaQuery, int limit) {
        UUID applicationId = getApplicationId(applicationName);
        LimitedScanResult<List<Dot>> r1 = applicationTraceIndexDao.scanScatterData(applicationName, dragAreaQuery, limit);
        LimitedScanResult<List<Dot>> r2 = applicationTraceIndexDaoV2.scanScatterData(applicationId, dragAreaQuery, limit);
        return merge(r1, r2);
    }

    @Override
    public LimitedScanResult<List<DotMetaData>> scanScatterDataV2(String applicationName, DragAreaQuery dragAreaQuery, int limit) {
        UUID applicationId = getApplicationId(applicationName);
        LimitedScanResult<List<DotMetaData>> r1 = applicationTraceIndexDao.scanScatterDataV2(applicationName, dragAreaQuery, limit);
        LimitedScanResult<List<DotMetaData>> r2 = applicationTraceIndexDaoV2.scanScatterDataV2(applicationId, dragAreaQuery, limit);
        return merge(r1, r2);
    }

    private UUID getApplicationId(String applicationName) {
        return this.applicationInfoService.getApplicationId(applicationName);
    }

    private <T> LimitedScanResult<List<T>> merge(LimitedScanResult<List<T>> r1, LimitedScanResult<List<T>> r2) {
        long limitedTime = Math.max(r1.limitedTime(), r2.limitedTime());
        List<T> scanData = mergeList(r1.scanData(), r2.scanData());
        return new LimitedScanResult<>(limitedTime, scanData);
    }

    private <T> List<T> mergeList(List<T> l1, List<T> l2) {
        List<T> result = new ArrayList<>(l1.size() + l2.size());
        result.addAll(l1);
        result.addAll(l2);
        return result;
    }

}
