package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.SqlUidMetaDataDao;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HbaseSqlUidMetaDataService implements SqlUidMetaDataService {
    private final SqlUidMetaDataDao sqlUidMetaDataDao;

    public HbaseSqlUidMetaDataService(SqlUidMetaDataDao sqlUidMetaDataDao) {
        this.sqlUidMetaDataDao = Objects.requireNonNull(sqlUidMetaDataDao, "sqlUidMetaDataDao");
    }

    @Override
    public void insert(SqlUidMetaDataBo sqlUidMetaDataBo) {
        this.sqlUidMetaDataDao.insert(sqlUidMetaDataBo);
    }
}
