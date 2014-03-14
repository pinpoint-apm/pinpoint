package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.view.AgentResponseTimeViewModelList;
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

    // ApplicationLevelHistogram
    private final Histogram applicationHistogram;

    // key는 agentId이다.
    private final Map<String, Histogram> agentHistogramMap;

    private final ApplicationTimeSeriesHistogram applicationTimeSeriesHistogram;

    private final AgentTimeSeriesHistogram agentTimeSeriesHistogram;


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

        this.applicationHistogram = new Histogram(this.application.getServiceType());
        this.agentHistogramMap = new HashMap<String, Histogram>();

        this.applicationTimeSeriesHistogram = new ApplicationTimeSeriesHistogram(this.application, this.range);
        this.agentTimeSeriesHistogram = new AgentTimeSeriesHistogram(this.application, this.range);
    }

    public ResponseHistogramSummary(Application application, Range range, List<ResponseTime> responseHistogramList) {
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

        this.agentTimeSeriesHistogram = createAgentLevelTimeSeriesResponseTime(responseHistogramList);
        this.applicationTimeSeriesHistogram = createApplicationLevelTimeSeriesResponseTime(responseHistogramList);

        this.agentHistogramMap = createAgentLevelResponseTime(responseHistogramList);
        this.applicationHistogram = createApplicationLevelResponseTime(responseHistogramList);

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



    public AgentResponseTimeViewModelList getAgentTimeSeriesHistogram() {
        return new AgentResponseTimeViewModelList(agentTimeSeriesHistogram.createViewModel());
    }


    private ApplicationTimeSeriesHistogram createApplicationLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
        ApplicationTimeSeriesHistogramBuilder builder = new ApplicationTimeSeriesHistogramBuilder(application, range);
        return builder.build(responseHistogramList);
    }


    private AgentTimeSeriesHistogram createAgentLevelTimeSeriesResponseTime(List<ResponseTime> responseHistogramList) {
        AgentTimeSeriesHistogram histogram = new AgentTimeSeriesHistogram(application, range);
        histogram.build(responseHistogramList);
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
