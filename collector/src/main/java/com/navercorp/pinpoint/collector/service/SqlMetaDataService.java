package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;

import javax.validation.Valid;

public interface SqlMetaDataService {
    void insert(@Valid SqlMetaDataBo sqlMetaDataBo);
}
