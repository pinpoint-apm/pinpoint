package com.nhn.pinpoint.server.dao;

import com.nhn.pinpoint.common.dto2.thrift.SqlMetaData;

/**
 *
 */
public interface SqlMetaDataDao {
    void insert(SqlMetaData sqlMetaData);
}
