/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component
public class AgentEventResultsExtractor implements ResultsExtractor<List<AgentEventBo>> {

    @Autowired
    @Qualifier("agentEventMapper")
    private RowMapper<List<AgentEventBo>> agentEventMapper;

    @Override
    public List<AgentEventBo> extractData(ResultScanner results) throws Exception {
        List<AgentEventBo> agentEvents = new ArrayList<>();
        int rowNum = 0;
        for (Result result : results) {
            List<AgentEventBo> intermediateEvents = agentEventMapper.mapRow(result, rowNum++);
            if (!intermediateEvents.isEmpty()) {
                agentEvents.addAll(intermediateEvents);
            }
        }
        return agentEvents;
    }
}