package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.thrift.dto.TApiMetaData;

/**
 * @author emeroad
 */
public interface ApiMetaDataDao {

    void insert(TApiMetaData apiMetaData);
}
