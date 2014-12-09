package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.bo.StringMetaDataBo;

import java.util.List;

/**
 * @author emeroad
 */
public interface StringMetaDataDao {
    List<StringMetaDataBo> getStringMetaData(String agentId, long time, int stringId);
}
