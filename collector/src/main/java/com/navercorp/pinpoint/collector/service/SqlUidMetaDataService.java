package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import jakarta.validation.Valid;

public interface SqlUidMetaDataService {
    void insert(@Valid SqlUidMetaDataBo sqlUidMetaDataBo);
}
