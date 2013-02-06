package com.nhn.hippo.web.dao;

import java.util.List;

import com.profiler.common.bo.ApiMetaDataBo;

/**
 *
 */
public interface ApiMetaDataDao {
    List<ApiMetaDataBo> getApiMetaData(String agentId, int apiId, long time);
}
