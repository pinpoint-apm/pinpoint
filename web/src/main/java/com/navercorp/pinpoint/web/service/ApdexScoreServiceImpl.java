package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogramBuilder;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorData;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentApdexScoreChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationApdexScoreChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DoubleApplicationStatPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class ApdexScoreServiceImpl implements ApdexScoreService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final MapResponseDao mapResponseDao;

    public ApdexScoreServiceImpl(MapResponseDao mapResponseDao) {
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
    }

    private AgentTimeHistogram createAgentTimeHistogram(Application application, Range range, TimeWindow timeWindow, List<ResponseTime> responseHistogramList) {
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(application, range, timeWindow);
        AgentTimeHistogram timeHistogram = builder.build(responseHistogramList);
        return timeHistogram;
    }

    @Override
    public ApdexScore selectApdexScoreData(Application application, Range range) {
        ServiceType applicationServiceType = application.getServiceType();

        if (applicationServiceType.isWas()) {
            List<ResponseTime> responseTimeList = mapResponseDao.selectResponseTime(application, range);
            Histogram applicationHistogram = createApplicationHistogram(responseTimeList, applicationServiceType);

            return ApdexScore.newApdexScore(applicationHistogram);
        } else {
            logger.debug("application service type isWas:{}", applicationServiceType.isWas());
            return ApdexScore.newApdexScore(new Histogram(applicationServiceType));
        }
    }

    private Histogram createApplicationHistogram(List<ResponseTime> responseHistogram, ServiceType applicationServiceType) {
        final Histogram applicationHistogram = new Histogram(applicationServiceType);
        for (ResponseTime responseTime : responseHistogram) {
            final Collection<TimeHistogram> histogramList = responseTime.getAgentResponseHistogramList();
            for (Histogram histogram : histogramList) {
                applicationHistogram.add(histogram);
            }
        }
        return applicationHistogram;
    }

    @Override
    public StatChart selectApplicationChart(Application application, Range range, TimeWindow timeWindow){
        List<ResponseTime> responseTimeList = mapResponseDao.selectResponseTime(application, range);
        AgentTimeHistogram timeHistogram = createAgentTimeHistogram(application, range, timeWindow, responseTimeList);

        List<DoubleApplicationStatPoint> applicationStatPoints = timeHistogram.getApplicationApdexScoreList(timeWindow);

        return new ApplicationApdexScoreChart(timeWindow, applicationStatPoints);
    }

    @Override
    public StatChart selectAgentChart(Application application, Range range, TimeWindow timeWindow, String agentId){
        List<ResponseTime> responseTimeList = mapResponseDao.selectResponseTime(application, range);
        AgentTimeHistogram timeHistogram = createAgentTimeHistogram(application, range, timeWindow, responseTimeList);

        List<SampledApdexScore> sampledPoints = timeHistogram.getSampledAgentApdexScoreList(agentId);
        return new AgentApdexScoreChart(timeWindow, sampledPoints);
    }

    @Override
    public InspectorData selectApplicationInspectorData(Application application, Range range, TimeWindow timeWindow) {
          List<ResponseTime> responseTimeList = mapResponseDao.selectResponseTime(application, range);
          AgentTimeHistogram timeHistogram = createAgentTimeHistogram(application, range, timeWindow, responseTimeList);
          List<DoubleApplicationStatPoint> applicationStatPoints = timeHistogram.getApplicationApdexScoreList(timeWindow);

          ApplicationApdexScoreChart chart = new ApplicationApdexScoreChart(timeWindow, applicationStatPoints);
          return chart.getInspectorData(timeWindow, applicationStatPoints);
    }

    @Override
    public InspectorData selectAgentInspectorData(Application application, Range range, TimeWindow timeWindow, String agentId) {
        List<ResponseTime> responseTimeList = mapResponseDao.selectResponseTime(application, range);
        AgentTimeHistogram timeHistogram = createAgentTimeHistogram(application, range, timeWindow, responseTimeList);
        List<SampledApdexScore> sampledPoints = timeHistogram.getSampledAgentApdexScoreList(agentId);

        AgentApdexScoreChart chart = new AgentApdexScoreChart(timeWindow, sampledPoints);
        return chart.getInspectorData(timeWindow, sampledPoints);
    }
}
