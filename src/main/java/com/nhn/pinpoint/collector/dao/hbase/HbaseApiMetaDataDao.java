package com.nhn.pinpoint.collector.dao.hbase;

import com.nhn.pinpoint.common.bo.ApiMetaDataBo;
import com.nhn.pinpoint.common.dto2.thrift.ApiMetaData;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.collector.dao.ApiMetaDataDao;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 */
@Repository
public class HbaseApiMetaDataDao implements ApiMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(ApiMetaData apiMetaData) {
        if (logger.isDebugEnabled()) {
            logger.debug("insert:" + apiMetaData);
        }


        ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo(apiMetaData.getAgentId(), apiMetaData.getApiId(), apiMetaData.getAgentStartTime());
        byte[] rowKey = apiMetaDataBo.toRowKey();

        Put put = new Put(rowKey);
        String api = apiMetaData.getApiInfo();
        int bufferSize = api.length() + 4 + 4;
        Buffer buffer = new FixedBuffer(bufferSize);
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
