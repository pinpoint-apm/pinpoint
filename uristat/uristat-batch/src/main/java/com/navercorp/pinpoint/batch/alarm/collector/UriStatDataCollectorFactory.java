/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm.collector;

import com.navercorp.pinpoint.batch.alarm.checker.UriStatAlarmChecker;
import com.navercorp.pinpoint.batch.alarm.dao.UriStatDao;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UriStatDataCollectorFactory {
    private final UriStatDao uriStatDao;
    private final String tenantId;
    private final Map<UriStatAlarmChecker, PinotDataCollector> collectorMap = new ConcurrentHashMap<>();

    public UriStatDataCollectorFactory(TenantProvider tenantProvider, UriStatDao uriStatDao) {
        Objects.requireNonNull(tenantProvider, "tenantProvider");
        this.uriStatDao = Objects.requireNonNull(uriStatDao, "uriStatDao");
        this.tenantId = tenantProvider.getTenantId();
    }

    public PinotDataCollector getDataCollector(UriStatAlarmChecker checker) {
        System.out.println("???? " + checker.name());
        return collectorMap.computeIfAbsent(checker, k -> createDataCollector(checker));
    }

    private PinotDataCollector createDataCollector(UriStatAlarmChecker checker) {
        if (! (checker instanceof UriStatAlarmChecker)) {
            throw new IllegalArgumentException("checker not an instance of UriStatAlarmCheckers");
        }

        switch (checker) {
            case TOTAL_COUNT:
                return new TotalCountDataCollector(tenantId, uriStatDao);
            case FAILURE_COUNT:
                return new FailureCountDataCollector(tenantId, uriStatDao);
            case APDEX:
                return new ApdexDataCollector(tenantId, uriStatDao);
            case AVG_RESPONSE_MS:
                return new AvgResponseDataCollector(tenantId, uriStatDao);
            case MAX_RESPONES_MS:
                return new MaxResponseDataCollector(tenantId, uriStatDao);
        }

        throw new IllegalArgumentException("unable to create DataCollector : " + checker);
    }
}
