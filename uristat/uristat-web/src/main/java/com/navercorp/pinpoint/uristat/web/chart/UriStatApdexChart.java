package com.navercorp.pinpoint.uristat.web.chart;

import com.navercorp.pinpoint.uristat.web.dao.UriStatChartDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class UriStatApdexChart extends UriStatChartType {
    private final static List<String> fieldNames = Arrays.asList("apdex");

    public UriStatApdexChart(@Qualifier("pinotApdexChartDao") UriStatChartDao chartDao) {
        this.type = "apdex";
        this.chartDao = Objects.requireNonNull(chartDao, "chartDao");
    }

    @Override
    public List<String> getFieldNames() {
        return fieldNames;
    }

    @Override
    public UriStatChartDao getChartDao() {
        return chartDao;
    }
}
