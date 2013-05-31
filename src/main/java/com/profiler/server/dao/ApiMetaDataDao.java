package com.profiler.server.dao;

import com.nhn.pinpoint.common.dto2.thrift.ApiMetaData;

/**
 *
 */
public interface ApiMetaDataDao {

    void insert(ApiMetaData apiMetaData);
}
