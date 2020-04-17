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

package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.List;

/**
 * @author emeroad
 * @author netspider
 */
public interface ApplicationTraceIndexDao {

    LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, Range range, int limit, boolean backwardDirection);

    ScatterData scanTraceScatterData(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean scanBackward);

}
