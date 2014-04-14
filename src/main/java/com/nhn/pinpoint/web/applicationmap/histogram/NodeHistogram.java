package com.nhn.pinpoint.web.applicationmap.histogram;

import com.nhn.pinpoint.web.view.AgentResponseTimeViewModelList;
import com.nhn.pinpoint.web.view.ResponseTimeViewModel;
import com.nhn.pinpoint.web.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

/**
 * applicationHistogram
 * agentHistogram
 * applicationTimeHistogram
 * agentTimeHistogram
 * 의 집합
 * @author emeroad
 */
public class NodeHistogram {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application application;

    private final Range range;

    // ApplicationLevelHistogram
    private Histogram applicationHistogram;

    // key는 agentId이다.
    private Map<String, Histogram> agentHistogramMap;

    private ApplicationTimeHistogram applicationTimeHistogram;

    private AgentTimeHistogram agentTimeHistogram;


    public NodeHistogram(Application application, Range range) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.application = application;
        this.range = range;

        this.applicationHistogram = new Histogram(this.application.getServiceType());
        this.agentHistogramMap = new HashMap<String, Histogram>();

        this.applicationTimeHistogram = new ApplicationTimeHistogram(this.application, this.range);
        this.agentTimeHistogram = new AgentTimeHistogram(this.application, this.range);
    }

    public NodeHistogram(Application application, Range range, List<ResponseTime> responseHistogramList) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (responseHistogramList == null) {
            throw new NullPointerException("responseHistogramList must not be null");
        }
        this.application = application;
        this.range = range;

        this.agentTimeHistogram = createAgentLevelTimeSeriesResponseTime(responseHistogramList);
        this.applicationTimeHistogram = createApplicationLevelTimeSeriesResponseTime(responseHistogramList);

        this.agentHistogramMap = createAgentLevelResponseTime(responseHistogramList);
        this.applicationHistogram = createApplicationLevelResponseTime(responseHistogramList);

    }


    public Histogram getApplicationHistogram() {
        return applicationHistogram;
    }

    public void setApplicationTimeHistogram(ApplicationTimeHistogram applicationTimeHistogram) {
        this.applicationTimeHistogram = applicationTimeHistogram;
    }

    public void setApplicationHistogram(Histogram applicationHistogram) {
        if (applicationHistogram == null) {
            throw new NullPointerException("applicationHistogram must not be null");
        }
        this.applicationHistogram = applicationHistogram;
    }

    public void setAgentHistogramMap(Map<String, Histogram> agentHistogramMap) {
        this.agentHistogramMap = agentHistogramMap;
    }

    public Map<String, Histogram> getAgentHistogramMap() {
        return agentHistogramMap;
    }

    public List<ResponseTimeViewModel> getApplicationTimeHistogram() {
        return applicationTimeHistogram.createViewModel();
    }


    public AgentResponseTimeViewModelList getAgentTimeHistogram() {
        return new AgentResponseTimeViewModelList(agentTimeHistogram.createViewModel());
    }

    public void setAgentTimeHistogram(AgentTimeHistogram agentTimeHistogram) {
        this.agentTimeHistogram = agentTimeHistogram;
    }

    private ApplicationTimeHistogram createApplicationLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(application, range);
        return builder.build(responseHistogramList);
    }


    private AgentTimeHistogram createAgentLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(application, range);
        AgentTimeHistogram histogram = builder.build(responseHistogramList);
        return histogram;
    }


    private Map<String, Histogram> createAgentLevelResponseTime(List<ResponseTime> responseHistogramList) {
        Map<String, Histogram> agentHistogramMap = new HashMap<String, Histogram>();
        for (ResponseTime responseTime : responseHistogramList) {
            for (Map.Entry<String, Histogram> entry : responseTime.getAgentHistogram()) {
                addAgentLevelHistogram(agentHistogramMap, entry.getKey(), entry.getValue());
            }
        }
        return agentHistogramMap;
    }

    private void addAgentLevelHistogram(Map<String, Histogram> agentHistogramMap, String agentId, Histogram histogram) {
        Histogram agentHistogram = agentHistogramMap.get(agentId);
        if (agentHistogram == null) {
            agentHistogram = new Histogram(application.getServiceType());
            agentHistogramMap.put(agentId, agentHistogram);
        }
        agentHistogram.add(histogram);
    }

    private Histogram createApplicationLevelResponseTime(List<ResponseTime> responseHistogram) {
        final Histogram applicationHistogram = new Histogram(this.application.getServiceType());
        for (ResponseTime responseTime : responseHistogram) {
            final Collection<Histogram> histogramList = responseTime.getAgentResponseHistogramList();
            for (Histogram histogram : histogramList) {
                applicationHistogram.add(histogram);
            }
        }
        return applicationHistogram;
    }
}
