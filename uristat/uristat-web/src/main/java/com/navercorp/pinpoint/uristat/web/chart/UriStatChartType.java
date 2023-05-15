package com.navercorp.pinpoint.uristat.web.chart;

import com.navercorp.pinpoint.uristat.web.dao.UriStatChartDao;

import java.util.List;

public abstract class UriStatChartType {
    protected String type;
    protected UriStatChartDao chartDao;

    public abstract List<String> getFieldNames();

    public abstract UriStatChartDao getChartDao();

    public String getType() {
        return type;
    }
}