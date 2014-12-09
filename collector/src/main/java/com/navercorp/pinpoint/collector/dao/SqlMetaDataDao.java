package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;

/**
 * @author emeroad
 */
public interface SqlMetaDataDao {
    void insert(TSqlMetaData sqlMetaData);
}
