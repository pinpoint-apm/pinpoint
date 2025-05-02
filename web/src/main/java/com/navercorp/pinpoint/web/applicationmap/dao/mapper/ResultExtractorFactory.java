package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;

@FunctionalInterface
public interface ResultExtractorFactory<R> {

    ResultsExtractor<R> newMapper(TimeWindowFunction timeWindow);
}
