package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;

public interface SqlMetaDataService {
    void insert(SqlMetaDataBo sqlMetaDataBo);
}
