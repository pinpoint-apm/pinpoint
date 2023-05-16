package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorData;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;

public interface ApdexScoreService {

    ApdexScore selectApdexScoreData(Application application, Range range);

    StatChart selectApplicationChart(Application application, Range range, TimeWindow timeWindow);

    StatChart selectAgentChart(Application application, Range range, TimeWindow timeWindow, String agentId);

    InspectorData selectApplicationInspectorData(Application application, Range range, TimeWindow timeWindow);

    InspectorData selectAgentInspectorData(Application application, Range range, TimeWindow timeWindow, String agentId);
}
