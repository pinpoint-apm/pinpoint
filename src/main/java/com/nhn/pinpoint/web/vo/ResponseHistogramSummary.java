package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.view.AgentResponseTimeViewModel;
import com.nhn.pinpoint.web.view.ResponseTimeViewModel;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.*;

/**
 * @author emeroad
 */
public class ResponseHistogramSummary {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final Application application;

    private final Range range;
    private final Histogram applicationHistogram;


    // key는 agentId이다.
    private Map<String, Histogram> agentHistogramMap = new HashMap<String, Histogram>();

    private ApplicationTimeSeriesHistogram applicationTimeSeriesHistogram;

    private AgentTimeSeriesHistogram agentTimeSeriesHistogram;


    // 현재 노가다 json으로 변경하는 부분이 많아 모양이 이쁘게 나오기 애매하므로 일단 static으로 생성해서하자.
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ResponseHistogramSummary(Application application, Range range) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.application = application;
        this.range = range;
        this.applicationHistogram = new Histogram(application.getServiceType());

        this.applicationTimeSeriesHistogram = new ApplicationTimeSeriesHistogram(application, range);
        this.agentTimeSeriesHistogram = new AgentTimeSeriesHistogram(application, range);
    }

    private void addApplicationLevelHistogram(Histogram histogram) {
        if (histogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        this.applicationHistogram.add(histogram);
    }

    public Histogram getApplicationHistogram() {
        return applicationHistogram;
    }

    public void addLinkHistogram(Histogram linkHistogram) {
        if (linkHistogram == null) {
            throw new NullPointerException("histogram must not be null");
        }
        this.applicationHistogram.addUncheckType(linkHistogram);
    }

    public Map<String, Histogram> getAgentHistogramMap() {
        return agentHistogramMap;
    }

    public List<ResponseTimeViewModel> getApplicationTimeSeriesHistogram() {
        return applicationTimeSeriesHistogram.createViewModel();
    }

    public String getApplicationTimeSeriesHistogramToJson() {
        try {
            List<ResponseTimeViewModel> viewModel = applicationTimeSeriesHistogram.createViewModel();
            return MAPPER.writeValueAsString(viewModel);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }


    public String getAgentTimeSeriesHistogramToJson() {
        try {
            List<AgentResponseTimeViewModel> viewModel = agentTimeSeriesHistogram.createViewModel();
            return MAPPER.writeValueAsString(viewModel);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }


    public void createResponseHistogram(List<ResponseTime> responseHistogramList) {
        createAgentLevelTimeSeriesResponseTime(responseHistogramList);
        createApplicationLevelTimeSeriesResponseTime(responseHistogramList);

        createAgentLevelResponseTime(responseHistogramList);
        createApplicationLevelResponseTime(responseHistogramList);

    }


    private void createApplicationLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {

        ApplicationTimeSeriesHistogram histogram = new ApplicationTimeSeriesHistogram(application, range);
        histogram.build(responseHistogramList);

        this.applicationTimeSeriesHistogram = histogram;

    }


    private void createAgentLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
        AgentTimeSeriesHistogram histogram = new AgentTimeSeriesHistogram(application, range);
        histogram.build(responseHistogramList);

        this.agentTimeSeriesHistogram = histogram;
    }


    private void createAgentLevelResponseTime(List<ResponseTime> responseHistogramList) {

        for (ResponseTime responseTime : responseHistogramList) {
            for (Map.Entry<String, Histogram> entry : responseTime.getAgentHistogram()) {
                addAgentLevelHistogram(entry.getKey(), entry.getValue());
            }
        }
    }

    private void addAgentLevelHistogram(String agentId, Histogram histogram) {
        Histogram agentHistogram = this.agentHistogramMap.get(agentId);
        if (agentHistogram == null) {
            agentHistogram = new Histogram(application.getServiceType());
            this.agentHistogramMap.put(agentId, agentHistogram);
        }
        agentHistogram.add(histogram);
    }

    private void createApplicationLevelResponseTime(List<ResponseTime> responseHistogram) {
        for (ResponseTime responseTime : responseHistogram) {
            final Collection<Histogram> histogramList = responseTime.getAgentResponseHistogramList();
            for (Histogram histogram : histogramList) {
                this.addApplicationLevelHistogram(histogram);
            }
        }
    }
}
