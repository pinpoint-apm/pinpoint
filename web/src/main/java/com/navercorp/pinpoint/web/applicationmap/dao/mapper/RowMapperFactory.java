package com.navercorp.pinpoint.web.applicationmap.dao.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowFunction;

@FunctionalInterface
public interface RowMapperFactory<R> {

    RowMapper<R> newMapper(TimeWindowFunction timeWindow);
}
