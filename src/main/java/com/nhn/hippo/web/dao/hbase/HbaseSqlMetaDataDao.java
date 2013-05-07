package com.nhn.hippo.web.dao.hbase;

import java.util.List;

import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.web.dao.SqlMetaDataDao;
import com.profiler.common.bo.SqlMetaDataBo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;

/**
 *
 */
@Repository
public class HbaseSqlMetaDataDao implements SqlMetaDataDao {

    @Autowired
    private HbaseOperations2 hbaseOperations2;

    @Autowired
    @Qualifier("sqlMetaDataMapper")
    private RowMapper<List<SqlMetaDataBo>> sqlMetaDataMapper;

    @Override
    public List<SqlMetaDataBo> getSqlMetaData(String agentId, int hashCode, long time) {
        SqlMetaDataBo sqlMetaData = new SqlMetaDataBo(agentId, hashCode, time);
        byte[] sqlId = sqlMetaData.toRowKey();

        Get get = new Get(sqlId);
        get.addFamily(HBaseTables.SQL_METADATA_CF_SQL);

        return hbaseOperations2.get(HBaseTables.SQL_METADATA, get, sqlMetaDataMapper);
    }
}
