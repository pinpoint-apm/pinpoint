package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.bo.SqlMetaDataBo;

import java.util.List;

/**
 * @author emeroad
 */
public interface SqlMetaDataDao {
    List<SqlMetaDataBo> getSqlMetaData(String agentId, long time, int hashCode);
}
