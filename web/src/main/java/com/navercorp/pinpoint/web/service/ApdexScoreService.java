package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;

public interface ApdexScoreService {

    ApdexScore selectApdexScoreData(Application application, TimeWindow timeWindow);

    ApdexScore selectApdexScoreData(Application application, String agentId, TimeWindow timeWindow);

    StatChart<?> selectApplicationChart(Application application, TimeWindow timeWindow);

    StatChart<?> selectAgentChart(Application application, TimeWindow timeWindow, String agentId);
}
