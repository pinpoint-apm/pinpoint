package com.profiler.server.dao.hbase;

import com.profiler.common.dto.thrift.SqlMetaData;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.RowKeyUtils;
import com.profiler.server.dao.SqlMetaDataDao;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class HbaseSqlMetaDataDao implements SqlMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(SqlMetaData sqlMetaData) {
        String agentId = sqlMetaData.getAgentId();
        if (logger.isDebugEnabled()) {
            logger.debug("insert:" + sqlMetaData);
        }

        byte[] rowKey = RowKeyUtils.getSqlId(agentId, sqlMetaData.getHashCode(), sqlMetaData.getStartTime());


        Put put = new Put(rowKey);
        String sql = sqlMetaData.getSql();
        byte[] sqlBytes = Bytes.toBytes(sql);
        put.add(HBaseTables.SQL_METADATA_CF_SQL, sqlBytes, null);

        hbaseTemplate.put(HBaseTables.SQL_METADATA, put);
    }
}
