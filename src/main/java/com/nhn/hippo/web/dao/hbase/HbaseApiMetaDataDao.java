package com.nhn.hippo.web.dao.hbase;

import com.nhn.hippo.web.dao.ApiMetaDataDao;
import com.nhn.hippo.web.mapper.ApiMetaDataMapper;
import com.profiler.common.bo.ApiMetaDataBo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.RowKeyUtils;
import org.apache.hadoop.hbase.client.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 */
@Repository
public class HbaseApiMetaDataDao implements ApiMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    private ApiMetaDataMapper mapper;

    @Override
    public List<ApiMetaDataBo> getApiMetaData(String agentId, int apiId, long time) {
        byte[] sqlId = RowKeyUtils.getApiId(agentId, apiId, time);
        Get get = new Get(sqlId);
        get.addFamily(HBaseTables.API_METADATA_CF_API);

        return hbaseOperations2.get(HBaseTables.API_METADATA, get, mapper);
    }
}
