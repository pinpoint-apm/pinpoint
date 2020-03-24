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

import com.navercorp.pinpoint.collector.dao.ApiMetaDataDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Repository
public class HbaseApiMetaDataDao implements ApiMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseTemplate;

    private final TableDescriptor<HbaseColumnFamily.ApiMetadata> description;

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    public HbaseApiMetaDataDao(HbaseOperations2 hbaseTemplate,
                               TableDescriptor<HbaseColumnFamily.ApiMetadata> description,
                               @Qualifier("metadataRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.description = Objects.requireNonNull(description, "description");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }

    @Override
    public void insert(ApiMetaDataBo apiMetaData) {
        Objects.requireNonNull(apiMetaData, "apiMetaData");
        if (logger.isDebugEnabled()) {
            logger.debug("insert:{}", apiMetaData);
        }

        // Assert agentId
        CollectorUtils.checkAgentId(apiMetaData.getAgentId());

        final byte[] rowKey = getDistributedKey(apiMetaData.toRowKey());
        final Put put = new Put(rowKey);
        final Buffer buffer = new AutomaticBuffer(64);
        final String api = apiMetaData.getApiInfo();
        buffer.putPrefixedString(api);
        buffer.putInt(apiMetaData.getLineNumber());
        buffer.putInt(apiMetaData.getMethodTypeEnum().getCode());

        final byte[] apiMetaDataBytes = buffer.getBuffer();
        put.addColumn(description.getColumnFamilyName(), description.getColumnFamily().QUALIFIER_SIGNATURE, apiMetaDataBytes);

        final TableName apiMetaDataTableName = description.getTableName();
        hbaseTemplate.put(apiMetaDataTableName, put);
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
}