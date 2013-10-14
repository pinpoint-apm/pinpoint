package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TStringMetaData;

/**
 *
 */
public interface StringMetaDataDao {

    void insert(TStringMetaData stringMetaData);
}
