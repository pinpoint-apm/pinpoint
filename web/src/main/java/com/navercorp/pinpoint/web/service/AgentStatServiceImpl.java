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

package com.navercorp.pinpoint.web.service;

import java.util.List;

import com.navercorp.pinpoint.web.dao.AgentStatDao;
import com.navercorp.pinpoint.web.vo.AgentStat;
import com.navercorp.pinpoint.web.vo.Range;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author harebox
 * @author hyungil.jeong
 */
@Service
public class AgentStatServiceImpl implements AgentStatService {

    @Autowired
    private AgentStatDao agentStatDao;

    public List<AgentStat> selectAgentStatList(String agentId, Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        return agentStatDao.getAgentStatList(agentId, range);
    }

}
