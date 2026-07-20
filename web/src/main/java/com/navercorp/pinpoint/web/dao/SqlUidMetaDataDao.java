package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;

import java.util.List;

public interface SqlUidMetaDataDao {
    List<SqlUidMetaDataBo> getSqlUidMetaData(String agentId, long time, byte[] sqlUid);

    /**
     * Batch variant of {@link #getSqlUidMetaData(String, long, byte[])}.
     * The returned list is index-aligned with {@code keys}; a key with no matching
     * row yields an empty list at the same index.
     */
    List<List<SqlUidMetaDataBo>> getSqlUidMetaData(List<SqlUidMetaDataKey> keys);

    record SqlUidMetaDataKey(String agentId, long agentStartTime, byte[] sqlUid) {
    }
}
