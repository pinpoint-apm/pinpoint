package com.nhn.hippo.web.dao;

import com.profiler.common.bo.SqlMetaDataBo;

import java.util.List;

/**
 *
 */
public interface SqlMetaDataDao {
    List<SqlMetaDataBo> getSqlMetaData(String agentId, short identifier, int hashCode, long time);
}
