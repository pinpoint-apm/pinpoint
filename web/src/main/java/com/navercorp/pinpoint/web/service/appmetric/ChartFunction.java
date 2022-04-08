package com.navercorp.pinpoint.web.service.appmetric;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;

import java.util.List;

@FunctionalInterface
public interface ChartFunction<IN extends AggregationStatData, OUT extends StatChart> {
    OUT apply(TimeWindow timeWindow, List<IN> applicationStatList);
}
