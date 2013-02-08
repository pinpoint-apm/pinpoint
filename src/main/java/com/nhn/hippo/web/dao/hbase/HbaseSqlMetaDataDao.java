package com.nhn.hippo.web.dao.hbase;

import com.nhn.hippo.web.dao.SqlMetaDataDao;
import com.nhn.hippo.web.mapper.SqlMetaDataMapper;
import com.profiler.common.bo.SqlMetaDataBo;
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
public class HbaseSqlMetaDataDao implements SqlMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    private SqlMetaDataMapper mapper;

    @Override
    public List<SqlMetaDataBo> getSqlMetaData(String agentId, short identifier, int hashCode, long time) {
        byte[] sqlId = RowKeyUtils.getSqlId(agentId, identifier, hashCode, time);
        Get get = new Get(sqlId);
        get.addFamily(HBaseTables.SQL_METADATA_CF_SQL);

        return hbaseOperations2.get(HBaseTables.SQL_METADATA, get, mapper);
    }
}
