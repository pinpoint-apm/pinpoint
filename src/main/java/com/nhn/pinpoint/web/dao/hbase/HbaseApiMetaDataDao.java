package com.nhn.pinpoint.web.dao.hbase;

import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.dao.ApiMetaDataDao;
import com.nhn.pinpoint.common.bo.ApiMetaDataBo;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;

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
    public List<ApiMetaDataBo> getApiMetaData(String agentId, int apiId, long time, int pid) {
        ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo(agentId, apiId, time);
        byte[] sqlId = apiMetaDataBo.toRowKey();
        Get get = new Get(sqlId);
        get.addFamily(HBaseTables.API_METADATA_CF_API);

        return hbaseOperations2.get(HBaseTables.API_METADATA, get, apiMetaDataMapper);
    }
}
