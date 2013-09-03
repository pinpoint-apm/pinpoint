package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.SqlMetaData;

/**
 *
 */
public interface SqlMetaDataDao {
    void insert(SqlMetaData sqlMetaData);
}
