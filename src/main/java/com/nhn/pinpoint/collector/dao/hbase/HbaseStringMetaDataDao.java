package com.nhn.pinpoint.collector.dao.hbase;

import com.nhn.pinpoint.collector.dao.StringMetaDataDao;
import com.nhn.pinpoint.common.bo.StringMetaDataBo;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.thrift.dto.TStringMetaData;
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
public class HbaseStringMetaDataDao implements StringMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(TStringMetaData stringMetaData) {
        if (logger.isDebugEnabled()) {
            logger.debug("insert:{}", stringMetaData);
        }

        final StringMetaDataBo sqlMetaDataBo = new StringMetaDataBo(stringMetaData.getAgentId(), stringMetaData.getStringId(), stringMetaData.getAgentStartTime());
        final byte[] rowKey = sqlMetaDataBo.toRowKey();


        Put put = new Put(rowKey);
        String stringValue = stringMetaData.getStringValue();
        byte[] sqlBytes = Bytes.toBytes(stringValue);
        // hashCode가 충돌날수 있으므로 일부러 qualifier에 넣음.
        put.add(HBaseTables.STRING_METADATA_CF_STR, sqlBytes, null);

        hbaseTemplate.put(HBaseTables.STRING_METADATA, put);
    }
}
