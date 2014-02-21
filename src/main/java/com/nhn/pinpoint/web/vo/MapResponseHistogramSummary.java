package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;

import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public class MapResponseHistogramSummary {

    private final Map<Application, ResponseHistogramSummary> responseHistogramSummaryMap = new HashMap<Application, ResponseHistogramSummary>();


    public void addHistogram(Application application, SpanBo span) {

        ResponseHistogramSummary responseHistogramSummary = getResponseHistogram(application);

        Histogram histogram = new Histogram(application.getServiceType());
        if (span.getErrCode() != 0) {
            histogram.addCallCountByElapsedTime(HistogramSchema.ERROR_SLOT_TIME);
        } else {
            histogram.addCallCountByElapsedTime(span.getElapsed());
        }
        responseHistogramSummary.addApplicationLevelHistogram(histogram);
        responseHistogramSummary.addAgentLevelHistogram(span.getAgentId(), histogram);
    }

    private ResponseHistogramSummary getResponseHistogram(Application application) {
        ResponseHistogramSummary responseHistogramSummary = responseHistogramSummaryMap.get(application);
        if (responseHistogramSummary == null) {
            responseHistogramSummary = new ResponseHistogramSummary(application);
            responseHistogramSummaryMap.put(application, responseHistogramSummary);
        }
        return responseHistogramSummary;
    }

    public ResponseHistogramSummary get(Application application) {
        return this.responseHistogramSummaryMap.get(application);
    }
}
