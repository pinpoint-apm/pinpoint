package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.common.bo.ApiMetaDataBo;

/**
 * @author emeroad
 */
public interface ApiMetaDataDao {
    List<ApiMetaDataBo> getApiMetaData(String agentId, long time, int apiId);
}
