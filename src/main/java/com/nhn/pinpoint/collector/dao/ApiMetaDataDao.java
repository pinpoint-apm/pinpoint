package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.ApiMetaData;

/**
 *
 */
public interface ApiMetaDataDao {

    void insert(ApiMetaData apiMetaData);
}
