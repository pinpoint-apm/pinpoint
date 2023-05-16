package com.navercorp.pinpoint.web.service.stat;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;

import java.util.List;

@FunctionalInterface
public interface SampledChartFunction<T extends SampledAgentStatDataPoint, OUT extends StatChart> {
    OUT apply(TimeWindow timeWindow, List<T> applicationStatList);
}
