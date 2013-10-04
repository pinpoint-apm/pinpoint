package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TApiMetaData;

/**
 *
 */
public interface ApiMetaDataDao {

    void insert(TApiMetaData apiMetaData);
}
