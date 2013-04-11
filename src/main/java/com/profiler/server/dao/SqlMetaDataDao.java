package com.profiler.server.dao;

import com.profiler.common.dto2.thrift.SqlMetaData;

/**
 *
 */
public interface SqlMetaDataDao {
    void insert(SqlMetaData sqlMetaData);
}
