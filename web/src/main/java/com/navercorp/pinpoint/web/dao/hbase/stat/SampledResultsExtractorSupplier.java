/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao.hbase.stat;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.util.TimeWindow;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
@FunctionalInterface
public interface SampledResultsExtractorSupplier<IN extends AgentStatDataPoint, OUT> {
    ResultsExtractor<List<OUT>> apply(TimeWindow timeWindow, AgentStatMapperV2<IN> mapper);
}
