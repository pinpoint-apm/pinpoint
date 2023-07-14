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

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.common.server.util.time.Range;

import java.util.List;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public interface FilteredMapService {

    LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit);

    LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit, boolean backwardDirection);

    ApplicationMap selectApplicationMap(FilteredMapServiceOption option);

    ApplicationMap selectApplicationMapWithScatterData(FilteredMapServiceOption option);

    ApplicationMap selectApplicationMapWithScatterDataV3(FilteredMapServiceOption option);
}
