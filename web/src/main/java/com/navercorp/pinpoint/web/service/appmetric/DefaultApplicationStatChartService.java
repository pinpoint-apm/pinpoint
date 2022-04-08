package com.navercorp.pinpoint.web.service.appmetric;

import com.navercorp.pinpoint.web.dao.appmetric.ApplicationMetricDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;

import java.util.List;
import java.util.Objects;

public class DefaultApplicationStatChartService<IN extends AggregationStatData, OUT extends StatChart> implements ApplicationStatChartService<OUT> {

    private final ApplicationMetricDao<IN> metricDao;
    private final ChartFunction<IN, OUT> chartFunction;

    public DefaultApplicationStatChartService(ApplicationMetricDao<IN> metricDao, ChartFunction<IN, OUT> chartFunction) {
        this.metricDao = Objects.requireNonNull(metricDao, "metricDao");
        this.chartFunction = Objects.requireNonNull(chartFunction, "chartFunction");
    }

    @Override
    public OUT selectApplicationChart(String applicationId, TimeWindow timeWindow) {
        Objects.requireNonNull(applicationId, "applicationId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<IN> applicationStatList = this.metricDao.getApplicationStatList(applicationId, timeWindow);
        return chartFunction.apply(timeWindow, applicationStatList);
    }

    @Override
    public String getChartType() {
        return metricDao.getChartType();
    }
}
