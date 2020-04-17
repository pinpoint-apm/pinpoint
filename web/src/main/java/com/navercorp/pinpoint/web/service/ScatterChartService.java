/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

import java.util.List;

public interface ScatterChartService {

    /**
     * Queries for data using filter
     *
     * @param traceIds
     * @param applicationName
     * @param filter
     * @return
     */
    List<Dot> selectScatterData(List<TransactionId> traceIds, String applicationName, Filter<SpanBo> filter);


    /**
     * Queries for scatter dots limited by the given limit.
     *
     * @param applicationName
     * @param from
     * @param to
     * @param limit
     * @return
     */
//  List<TransactionId> selectScatterTraceIdList(String applicationName, long from, long to, int limit);
    List<SpanBo> selectTransactionMetadata(List<GetTraceInfo> getTraceInfoList);

    ScatterData selectScatterData(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean backwardDirection);

    ScatterData selectScatterData(List<TransactionId> transactionIdList, String applicationName, Range range, int xGroupUnit, int yGroupUnit, Filter<SpanBo> filter);

}
