package com.nhn.hippo.web.dao;

import com.profiler.common.bo.ApiMetaDataBo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 */
public interface ApiMetaDataDao {
    List<ApiMetaDataBo> getApiMetaData(String agentId, int apiId, long time);
}
