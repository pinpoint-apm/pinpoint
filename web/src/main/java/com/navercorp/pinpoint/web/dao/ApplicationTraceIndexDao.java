/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SelectedScatterArea;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

import java.util.List;

/**
 * @author emeroad
 * @author netspider
 */
public interface ApplicationTraceIndexDao {

    LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, Range range, int limit, boolean backwardDirection);

    LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, SelectedScatterArea range, int limit);

    /**
     *
     * select transactions in a selection range(box) in the scatter chart.
     *
     * @param applicationName
     * @param area
     * @param offsetTransactionId
     * @param offsetTransactionElapsed
     * @param limit
     * @return
     */
    List<Dot> scanTraceScatter(String applicationName, SelectedScatterArea area, TransactionId offsetTransactionId, int offsetTransactionElapsed, int limit);

    ScatterData scanTraceScatterData(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean scanBackward);

}
