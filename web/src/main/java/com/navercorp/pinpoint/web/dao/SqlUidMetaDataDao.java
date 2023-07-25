package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;

import java.util.List;

public interface SqlUidMetaDataDao {
    List<SqlUidMetaDataBo> getSqlUidMetaData(String agentId, long time, byte[] sqlUid);
}
