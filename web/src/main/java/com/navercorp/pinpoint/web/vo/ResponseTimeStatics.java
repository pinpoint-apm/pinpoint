package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.histogram.StatisticsHistogram;

public record ResponseTimeStatics(long totalCount, long sumTime, long avgTime, long maxTime) {

    public static final String RESPONSE_STATISTICS = "responseStatistics";

    public static final String AGENT_RESPONSE_STATISTICS = "agentResponseStatistics";

    public static final String TOTAL_COUNT = "Tot";

    public static final String SUM_ELAPSED_TIME = "Sum";

    public static final String AVG_ELAPSED_TIME = "Avg";

    public static final String MAX_ELAPSED_TIME = "Max";

    public static ResponseTimeStatics fromHistogram(StatisticsHistogram histogram) {
        if (histogram == null) {
            return new ResponseTimeStatics(0, 0, 0, 0);
        }
        return new ResponseTimeStatics(histogram.getTotalCount(), histogram.getSumElapsed(), histogram.getAvgElapsed(), histogram.getMaxElapsed());
    }

    @JsonProperty(TOTAL_COUNT)
    @Override
    public long totalCount() {
        return totalCount;
    }

    @JsonProperty(SUM_ELAPSED_TIME)
    @Override
    public long sumTime() {
        return sumTime;
    }

    @JsonProperty(AVG_ELAPSED_TIME)
    @Override
    public long avgTime() {
        return avgTime;
    }

    @JsonProperty(MAX_ELAPSED_TIME)
    @Override
    public long maxTime() {
        return maxTime;
    }

    @Override
    public String toString() {
        return "ResponseTimeStatics{" +
                "totalCount=" + totalCount +
                ", sumTime=" + sumTime +
                ", avgTime=" + avgTime +
                ", maxTime=" + maxTime +
                '}';
    }
}
