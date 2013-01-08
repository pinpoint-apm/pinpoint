package com.nhn.hippo.web.dao;

import com.profiler.common.bo.SqlMetaDataBo;

import java.util.List;

/**
 *
 */
public interface SqlMetaDataDao {
    List<SqlMetaDataBo> getSqlMetaData(String agentId, int hashCode, long time);
}
