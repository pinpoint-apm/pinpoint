package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TSqlMetaData;

/**
 * @author emeroad
 */
public interface SqlMetaDataDao {
    void insert(TSqlMetaData sqlMetaData);
}
