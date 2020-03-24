/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.SqlMetaDataDao;
import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class HbaseSqlMetaDataDao implements SqlMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseOperations2 hbaseTemplate;

    private final TableDescriptor<HbaseColumnFamily.SqlMetadataV2> descriptor;

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    public HbaseSqlMetaDataDao(HbaseOperations2 hbaseTemplate,
                               TableDescriptor<HbaseColumnFamily.SqlMetadataV2> descriptor,
                               @Qualifier("metadataRowKeyDistributor2") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
    }

    @Override
    public void insert(SqlMetaDataBo sqlMetaData) {
        Objects.requireNonNull(sqlMetaData, "sqlMetaData");
        if (logger.isDebugEnabled()) {
            logger.debug("insert:{}", sqlMetaData);
        }

        // Assert agentId
        CollectorUtils.checkAgentId(sqlMetaData.getAgentId());

        final byte[] rowKey = getDistributedKey(sqlMetaData.toRowKey());
        final Put put = new Put(rowKey);
        final String sql = sqlMetaData.getSql();
        final byte[] sqlBytes = Bytes.toBytes(sql);
        put.addColumn(descriptor.getColumnFamilyName(), descriptor.getColumnFamily().QUALIFIER_SQLSTATEMENT, sqlBytes);

        final TableName sqlMetaDataTableName = descriptor.getTableName();
        hbaseTemplate.put(sqlMetaDataTableName, put);
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }


}
