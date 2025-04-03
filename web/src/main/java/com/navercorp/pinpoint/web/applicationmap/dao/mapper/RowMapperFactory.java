package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;

@FunctionalInterface
public interface RowMapperFactory<R> {

    RowMapper<R> newMapper(TimeWindowFunction timeWindow);
}
