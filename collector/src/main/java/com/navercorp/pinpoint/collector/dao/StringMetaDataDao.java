package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.thrift.dto.TStringMetaData;

/**
 * @author emeroad
 */
public interface StringMetaDataDao {

    void insert(TStringMetaData stringMetaData);
}
