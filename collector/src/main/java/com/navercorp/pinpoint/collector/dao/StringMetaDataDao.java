package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TStringMetaData;

/**
 * @author emeroad
 */
public interface StringMetaDataDao {

    void insert(TStringMetaData stringMetaData);
}
