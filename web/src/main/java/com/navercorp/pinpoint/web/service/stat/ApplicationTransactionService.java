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

import com.navercorp.pinpoint.web.dao.ApplicationMemoryDao;
import com.navercorp.pinpoint.web.dao.ApplicationTransactionDao;
import com.navercorp.pinpoint.web.service.ApplicationStatChartService;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ApplicationStatChartGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.ApplicationTransactionChartGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Service
public class ApplicationTransactionService implements ApplicationStatChartService {

    @Autowired
    private ApplicationTransactionDao applicationTransactionDao;

    @Override
    public ApplicationStatChartGroup selectApplicationChart(String applicationId, TimeWindow timeWindow) {
        if (applicationId == null) {
            throw new NullPointerException("applicationId must not be null");
        }
        if (timeWindow == null) {
            throw new NullPointerException("timeWindow must not be null");
        }
        List<AggreJoinTransactionBo> aggreJoinTransactionBoList = this.applicationTransactionDao.getApplicationStatList(applicationId, timeWindow);
        return new ApplicationTransactionChartGroup(timeWindow, aggreJoinTransactionBoList);
    }
}
