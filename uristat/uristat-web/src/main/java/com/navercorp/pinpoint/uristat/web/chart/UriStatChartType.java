package com.navercorp.pinpoint.uristat.web.chart;

import com.navercorp.pinpoint.uristat.web.dao.UriStatChartDao;

import java.util.List;

public interface UriStatChartType {

    List<String> getFieldNames();

    UriStatChartDao getChartDao();

    String getType();
}