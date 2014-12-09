package com.navercorp.pinpoint.web.dao;

import java.util.List;

import com.navercorp.pinpoint.common.bo.ApiMetaDataBo;

/**
 * @author emeroad
 */
public interface ApiMetaDataDao {
    List<ApiMetaDataBo> getApiMetaData(String agentId, long time, int apiId);
}
