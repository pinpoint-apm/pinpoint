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
package com.navercorp.pinpoint.web.service.appmetric;

import com.navercorp.pinpoint.web.dao.appmetric.ApplicationMetricDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinCpuLoadBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationCpuLoadChart;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
public class ApplicationCpuLoadService implements ApplicationStatChartService {

    private final ApplicationMetricDao<AggreJoinCpuLoadBo> metricDao;

    public ApplicationCpuLoadService(ApplicationMetricDao<AggreJoinCpuLoadBo> metricDao) {
        this.metricDao = Objects.requireNonNull(metricDao, "metricDao");
    }


    @Override
    public StatChart selectApplicationChart(String applicationId, TimeWindow timeWindow) {
        Objects.requireNonNull(applicationId, "applicationId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<AggreJoinCpuLoadBo> aggreJoinCpuLoadBoList = this.metricDao.getApplicationStatList(applicationId, timeWindow);
        return new ApplicationCpuLoadChart(timeWindow, aggreJoinCpuLoadBoList);
    }
}
