package com.navercorp.pinpoint.uristat.web.chart;

import com.navercorp.pinpoint.uristat.web.dao.UriStatChartDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class UriStatTotalChart extends UriStatChartType {
    private final static List<String> fieldNames = Arrays.asList("0 ~ 100ms", "100 ~ 300ms", "300 ~ 500ms", "500 ~ 1000ms", "1000 ~ 3000ms", "3000 ~ 5000ms", "5000 ~ 8000ms", "8000ms ~");

    public UriStatTotalChart(@Qualifier("pinotTotalCountChartDao") UriStatChartDao chartDao) {
        this.type = "total";
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
