package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowDownSampler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class ResponseHistogramBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TimeWindow window;

    private Map<Long, Map<Application, ResponseTime>> responseTimeApplicationMap = new HashMap<Long, Map<Application, ResponseTime>>();
    private Map<Application, List<ResponseTime>> result = new HashMap<Application, List<ResponseTime>>();


    public ResponseHistogramBuilder(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        // 일단 샘플링을 하지 않도록한다.
        this.window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);

    }

    public void addHistogram(Application application, SpanBo span, long timeStamp) {
        timeStamp = window.refineTimestamp(timeStamp);


        final ResponseTime responseTime = getResponseTime(application, timeStamp);
        if (span.getErrCode() != 0) {
            responseTime.addResponseTime(span.getAgentId(), HistogramSchema.ERROR_SLOT_TIME);
        } else {
            responseTime.addResponseTime(span.getAgentId(), span.getElapsed());
        }

    }


    public void addLinkHistogram(Application application, String agentId, TimeHistogram timeHistogram) {
        long timeStamp = timeHistogram.getTimeStamp();
        timeStamp = window.refineTimestamp(timeStamp);
        final ResponseTime responseTime = getResponseTime(application, timeStamp);
        responseTime.addResponseTime(agentId, timeHistogram);
    }

    private ResponseTime getResponseTime(Application application, Long timeStamp) {
        Map<Application, ResponseTime> responseTimeMap = responseTimeApplicationMap.get(timeStamp);
        if (responseTimeMap == null) {
            responseTimeMap = new HashMap<Application, ResponseTime>();
            responseTimeApplicationMap.put(timeStamp, responseTimeMap);
        }
        ResponseTime responseTime = responseTimeMap.get(application);
        if (responseTime == null) {
            responseTime = new ResponseTime(application.getName(), application.getServiceTypeCode(), timeStamp);
            responseTimeMap.put(application, responseTime);
        }
        return responseTime;
    }

    public void build() {
        final Map<Application, List<ResponseTime>> result = new HashMap<Application, List<ResponseTime>>();

        for (Map<Application, ResponseTime> entry : responseTimeApplicationMap.values()) {
            for (Map.Entry<Application, ResponseTime> applicationResponseTimeEntry : entry.entrySet()) {
                List<ResponseTime> responseTimeList = result.get(applicationResponseTimeEntry.getKey());
                if (responseTimeList == null) {
                    responseTimeList = new ArrayList<ResponseTime>();
                    Application key = applicationResponseTimeEntry.getKey();
                    result.put(key, responseTimeList);
                }
                responseTimeList.add(applicationResponseTimeEntry.getValue());
            }
        }

        this.responseTimeApplicationMap = null;
        this.result = result;

    }

    public List<ResponseTime> getResponseTimeList(Application application) {
        List<ResponseTime> responseTimes = this.result.get(application);
        return responseTimes;
    }


}
