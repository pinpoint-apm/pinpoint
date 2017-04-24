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

package com.navercorp.pinpoint.web.service.stat;

import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.web.dao.stat.ResponseTimeDao;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Service
public class ResponseTimeService implements AgentStatService<ResponseTimeBo> {

    private final ResponseTimeDao responseTimeDao;

    @Autowired
    public ResponseTimeService(@Qualifier("responseTimeDaoFactory") ResponseTimeDao responseTimeDao) {
        this.responseTimeDao = responseTimeDao;
    }

    @Override
    public List<ResponseTimeBo> selectAgentStatList(String agentId, Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        return this.responseTimeDao.getAgentStatList(agentId, range);
    }

}
