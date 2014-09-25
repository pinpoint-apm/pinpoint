package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.common.bo.SqlMetaDataBo;
import com.nhn.pinpoint.common.bo.StringMetaDataBo;

import java.util.List;

/**
 * @author emeroad
 */
public interface StringMetaDataDao {
    List<StringMetaDataBo> getStringMetaData(String agentId, long time, int stringId);
}
