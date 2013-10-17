package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.common.bo.SqlMetaDataBo;

import java.util.List;

/**
 *
 */
public interface SqlMetaDataDao {
    List<SqlMetaDataBo> getSqlMetaData(String agentId, long time, int hashCode);
}
