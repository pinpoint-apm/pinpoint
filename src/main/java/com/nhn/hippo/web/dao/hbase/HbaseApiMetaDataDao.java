package com.nhn.hippo.web.dao.hbase;

import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.web.dao.ApiMetaDataDao;
import com.profiler.common.bo.ApiMetaDataBo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.RowKeyUtils;

/**
 *
 */
@Repository
public class HbaseApiMetaDataDao implements ApiMetaDataDao {

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    @Qualifier("apiMetaDataMapper")
    private RowMapper<List<ApiMetaDataBo>> apiMetaDataMapper;

    @Override
    public List<ApiMetaDataBo> getApiMetaData(String agentId, short identifier, int apiId, long time) {
        byte[] sqlId = RowKeyUtils.getApiId(agentId, identifier, apiId, time);
        Get get = new Get(sqlId);
        get.addFamily(HBaseTables.API_METADATA_CF_API);

        return hbaseOperations2.get(HBaseTables.API_METADATA, get, apiMetaDataMapper);
    }
}
