/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.web.dao.ApplicationContainerDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinContainerBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationContainerChart;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Service
public class ApplicationContainerService implements ApplicationStatChartService {

    private final ApplicationContainerDao applicationContainerDao;

    public ApplicationContainerService(ApplicationContainerDao applicationContainerDao){
        this.applicationContainerDao = Objects.requireNonNull(applicationContainerDao, "applicationContainerDao");
    }

    @Override
    public StatChart selectApplicationChart(String applicationId, TimeWindow timeWindow) {
        Objects.requireNonNull(applicationId, "applicationId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<AggreJoinContainerBo> aggreJoinContainerBoList = this.applicationContainerDao.getApplicationStatList(applicationId, timeWindow);

        return new ApplicationContainerChart(timeWindow, aggreJoinContainerBoList);
    }
}
