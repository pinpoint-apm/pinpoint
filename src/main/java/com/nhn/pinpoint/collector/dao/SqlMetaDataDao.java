package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.common.dto2.thrift.SqlMetaData;

/**
 *
 */
public interface SqlMetaDataDao {
    void insert(SqlMetaData sqlMetaData);
}
