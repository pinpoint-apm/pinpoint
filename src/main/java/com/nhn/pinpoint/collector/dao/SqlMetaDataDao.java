package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TSqlMetaData;

/**
 *
 */
public interface SqlMetaDataDao {
    void insert(TSqlMetaData sqlMetaData);
}
