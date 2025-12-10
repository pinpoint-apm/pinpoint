/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.scatter.dao;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.scatter.vo.Dot;
import com.navercorp.pinpoint.web.scatter.vo.DotMetaData;

import java.util.List;

// ApplicationTraceIndexDao V2
public interface TraceIndexDao {

    boolean hasTraceIndex(int serviceUid, String applicationName, int serviceTypeCode, Range range, boolean backwardDirection);

    LimitedScanResult<List<DotMetaData>> scanTraceIndex(int serviceUid, String applicationName, int serviceTypeCode, Range range, int limit, boolean backwardDirection);

    LimitedScanResult<List<Dot>> scanTraceScatterData(int serviceUid, String applicationName, int serviceTypeCode, Range range, int limit, boolean scanBackward);

    LimitedScanResult<List<DotMetaData>> scanScatterDataV2(int serviceUid, String applicationName, int serviceTypeCode, DragAreaQuery dragAreaQuery, String rpcRegex, int limit);

}
