/*
 * Copyright 2014 Naver Corp.
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

package com.navercorp.pinpoint.web.dao.stat;

import java.util.List;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.service.stat.ChartTypeSupport;
import org.springframework.stereotype.Repository;

/**
 * @author HyunGil Jeong
 */
@Repository
public interface AgentStatDao<T extends AgentStatDataPoint> extends ChartTypeSupport {

    List<T> getAgentStatList(String agentId, Range range);

    boolean agentStatExists(String agentId, Range range);
}
