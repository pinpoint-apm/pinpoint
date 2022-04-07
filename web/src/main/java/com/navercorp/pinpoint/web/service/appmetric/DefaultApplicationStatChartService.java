package com.navercorp.pinpoint.web.service.appmetric;

import com.navercorp.pinpoint.web.dao.appmetric.ApplicationMetricDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;

import java.util.List;
import java.util.Objects;

public class DefaultApplicationStatChartService<T extends AggregationStatData> implements ApplicationStatChartService {

    private final ApplicationMetricDao<T> metricDao;
    private final ChartFunction<T> chartFunction;

    public DefaultApplicationStatChartService(ApplicationMetricDao<T> metricDao, ChartFunction<T> chartFunction) {
        this.metricDao = Objects.requireNonNull(metricDao, "metricDao");
        this.chartFunction = Objects.requireNonNull(chartFunction, "chartFunction");
    }

    @Override
    public StatChart selectApplicationChart(String applicationId, TimeWindow timeWindow) {
        Objects.requireNonNull(applicationId, "applicationId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<T> applicationStatList = this.metricDao.getApplicationStatList(applicationId, timeWindow);
        return chartFunction.apply(timeWindow, applicationStatList);
    }
}
