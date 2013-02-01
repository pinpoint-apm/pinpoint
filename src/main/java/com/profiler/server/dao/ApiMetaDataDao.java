package com.profiler.server.dao;

import com.profiler.common.dto.thrift.ApiMetaData;
import com.profiler.common.dto.thrift.SqlMetaData;

/**
 *
 */
public interface ApiMetaDataDao {

    void insert(ApiMetaData apiMetaData);
}
