package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.common.bo.ApiMetaDataBo;

/**
 *
 */
public interface ApiMetaDataDao {
    List<ApiMetaDataBo> getApiMetaData(String agentId, int apiId, long time);
}
