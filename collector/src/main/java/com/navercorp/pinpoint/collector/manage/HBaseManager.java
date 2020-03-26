/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.manage;

import com.navercorp.pinpoint.common.hbase.HBaseAsyncOperation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class HBaseManager extends AbstractCollectorManager implements HBaseManagerMBean {

    private final HBaseAsyncOperation hBaseAsyncOperation;

    public HBaseManager(HBaseAsyncOperation hBaseAsyncOperation) {
        this.hBaseAsyncOperation = Objects.requireNonNull(hBaseAsyncOperation, "hBaseAsyncOperation");
    }

    @Override
    public Long getAsyncOpsCount() {
        return hBaseAsyncOperation.getOpsCount();
    }

    @Override
    public Long getAsyncOpsRejectedCount() {
        return hBaseAsyncOperation.getOpsRejectedCount();
    }

    @Override
    public Map<String, Long> getCurrentAsyncOpsCountForEachRegionServer() {
        return hBaseAsyncOperation.getCurrentOpsCountForEachRegionServer();
    }

    @Override
    public Map<String, Long> getAsyncOpsFailedCountForEachRegionServer() {
        return hBaseAsyncOperation.getOpsFailedCountForEachRegionServer();
    }

    @Override
    public Map<String, Long> getAsyncOpsAverageLatencyForEachRegionServer() {
        return hBaseAsyncOperation.getOpsAverageLatencyForEachRegionServer();
    }

}
