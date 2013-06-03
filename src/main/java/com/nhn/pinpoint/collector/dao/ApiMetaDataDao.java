package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.common.dto2.thrift.ApiMetaData;

/**
 *
 */
public interface ApiMetaDataDao {

    void insert(ApiMetaData apiMetaData);
}
