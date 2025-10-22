/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentResponse;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentApdexScoreChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationApdexScoreChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationStatPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ApdexScoreServiceImpl implements ApdexScoreService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final MapAgentResponseDao mapAgentResponseDao;
    private final MapResponseDao mapResponseDao;

    public ApdexScoreServiceImpl(MapAgentResponseDao mapAgentResponseDao, MapResponseDao mapResponseDao) {
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
    }

    @Override
    public ApdexScore selectApdexScoreData(Application application, TimeWindow timeWindow) {
        ServiceType applicationServiceType = application.getServiceType();

        if (applicationServiceType.isWas()) {
            ApplicationResponse response = mapResponseDao.selectApplicationResponse(application, timeWindow);
            Histogram applicationHistogram = response.getApplicationTotalHistogram();

            return ApdexScore.newApdexScore(applicationHistogram);
        } else {
            logger.debug("application service type isWas:{}", applicationServiceType.isWas());
            return ApdexScore.newApdexScore(new Histogram(applicationServiceType));
        }
    }

    @Override
    public ApdexScore selectApdexScoreData(Application application, String agentId, TimeWindow timeWindow) {
        ServiceType applicationServiceType = application.getServiceType();

        if (applicationServiceType.isWas()) {
            AgentResponse agentHistogramList = mapAgentResponseDao.selectAgentResponse(application, timeWindow);
            Application searchAgent = new Application(agentId, application.getServiceType());
            Histogram agentHistogram = agentHistogramList.getAgentTotalHistogram(searchAgent);

            return ApdexScore.newApdexScore(agentHistogram);
        } else {
            logger.debug("application service type isWas:{}", applicationServiceType.isWas());
            return ApdexScore.newApdexScore(new Histogram(applicationServiceType));
        }
    }

    @Override
    public StatChart<?> selectApplicationChart(Application application, TimeWindow timeWindow) {
        List<ResponseTime> responseTimeList = mapAgentResponseDao.selectResponseTime(application, timeWindow);
        AgentTimeHistogram timeHistogram = createAgentTimeHistogram(application, timeWindow, responseTimeList);

        List<ApplicationStatPoint> applicationStatPoints = timeHistogram.getApplicationApdexScoreList(timeWindow);

        return new ApplicationApdexScoreChart(timeWindow, applicationStatPoints);
    }

    @Override
    public StatChart<?> selectAgentChart(Application application, TimeWindow timeWindow, String agentId) {
        List<ResponseTime> responseTimeList = mapAgentResponseDao.selectResponseTime(application, timeWindow);
        AgentTimeHistogram timeHistogram = createAgentTimeHistogram(application, timeWindow, responseTimeList);

        List<SampledApdexScore> sampledPoints = timeHistogram.getSampledAgentApdexScoreList(agentId);
        return new AgentApdexScoreChart(timeWindow, sampledPoints);
    }

    private AgentTimeHistogram createAgentTimeHistogram(Application application, TimeWindow timeWindow, List<ResponseTime> responseHistogramList) {
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(application, timeWindow);
        return builder.build(responseHistogramList);
    }
}
