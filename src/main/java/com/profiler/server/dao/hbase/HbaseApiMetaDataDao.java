package com.profiler.server.dao.hbase;

import com.profiler.common.dto.thrift.ApiMetaData;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.Buffer;
import com.profiler.common.util.RowKeyUtils;
import com.profiler.server.dao.ApiMetaDataDao;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class HbaseApiMetaDataDao implements ApiMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(ApiMetaData apiMetaData) {
        if (logger.isDebugEnabled()) {
            logger.debug("insert:" + apiMetaData);
        }

        String agentId = apiMetaData.getAgentId();
        byte[] rowKey = RowKeyUtils.getApiId(agentId, apiMetaData.getApiId(), apiMetaData.getStartTime());

        Put put = new Put(rowKey);
        String api = apiMetaData.getApiInfo();
        int bufferSize = api.length() + 4 + 4;
        Buffer buffer = new Buffer(bufferSize);
        buffer.putPrefixedBytes(Bytes.toBytes(api));
        if(apiMetaData.isSetLine()) {
            buffer.put(apiMetaData.getLine());
        } else {
            buffer.put(-1);
        }
        byte[] apiMetaDataBytes = buffer.getBuffer();
        put.add(HBaseTables.API_METADATA_CF_API, apiMetaDataBytes, null);

        hbaseTemplate.put(HBaseTables.API_METADATA, put);
    }
}
