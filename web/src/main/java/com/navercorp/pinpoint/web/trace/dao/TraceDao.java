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

package com.navercorp.pinpoint.web.trace.dao;


import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCount;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.web.service.FetchResult;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;

import java.util.List;

/**
 * @author emeroad
 */
public interface TraceDao {

    List<SpanBo> selectSpan(ServerTraceId transactionId);

    FetchResult<List<SpanBo>> selectSpan(ServerTraceId transactionId, ColumnGetCount columnGetCount);

    List<List<SpanBo>> selectSpans(List<GetTraceInfo> getTraceInfoList);
    
    List<List<SpanBo>> selectAllSpans(List<ServerTraceId> transactionIdList);

    List<List<SpanBo>> selectAllSpans(List<ServerTraceId> transactionIdList, ColumnGetCount columnGetCount);


}
