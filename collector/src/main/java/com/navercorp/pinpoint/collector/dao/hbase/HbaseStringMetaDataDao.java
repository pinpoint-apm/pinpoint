/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.StringMetaDataDao;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.thrift.dto.TStringMetaData;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Repository
public class HbaseStringMetaDataDao implements StringMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    @Qualifier("metadataRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    @Override
    public void insert(TStringMetaData stringMetaData) {
        if (stringMetaData == null) {
            throw new NullPointerException("stringMetaData must not be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("insert:{}", stringMetaData);
        }

        final StringMetaDataBo stringMetaDataBo = new StringMetaDataBo(stringMetaData.getAgentId(), stringMetaData.getAgentStartTime(), stringMetaData.getStringId());
        final byte[] rowKey = getDistributedKey(stringMetaDataBo.toRowKey());


        Put put = new Put(rowKey);
        String stringValue = stringMetaData.getStringValue();
        byte[] sqlBytes = Bytes.toBytes(stringValue);
        put.addColumn(HBaseTables.STRING_METADATA_CF_STR, HBaseTables.STRING_METADATA_CF_STR_QUALI_STRING, sqlBytes);

        hbaseTemplate.put(HBaseTables.STRING_METADATA, put);
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}
