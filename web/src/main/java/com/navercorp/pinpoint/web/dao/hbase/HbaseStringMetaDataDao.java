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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableDescriptor;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.web.dao.StringMetaDataDao;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
@Repository
public class HbaseStringMetaDataDao implements StringMetaDataDao {

    private final HbaseOperations2 hbaseOperations2;

    private final RowMapper<List<StringMetaDataBo>> stringMetaDataMapper;

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final TableDescriptor<HbaseColumnFamily.StringMetadataStr> descriptor;

    public HbaseStringMetaDataDao(HbaseOperations2 hbaseOperations2,
                                  TableDescriptor<HbaseColumnFamily.StringMetadataStr> descriptor,
                                  @Qualifier("stringMetaDataMapper") RowMapper<List<StringMetaDataBo>> stringMetaDataMapper,
                                  @Qualifier("metadataRowKeyDistributor") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.hbaseOperations2 = Objects.requireNonNull(hbaseOperations2, "hbaseOperations2");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.stringMetaDataMapper = Objects.requireNonNull(stringMetaDataMapper, "stringMetaDataMapper");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }

    @Override
    public List<StringMetaDataBo> getStringMetaData(String agentId, long time, int stringId) {
        Objects.requireNonNull(agentId, "agentId");

        StringMetaDataBo stringMetaData = new StringMetaDataBo(agentId, time, stringId);
        byte[] rowKey = getDistributedKey(stringMetaData.toRowKey());

        Get get = new Get(rowKey);
        get.addFamily(descriptor.getColumnFamilyName());

        TableName stringMetaDataTableName = descriptor.getTableName();
        return hbaseOperations2.get(stringMetaDataTableName, get, stringMetaDataMapper);
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }

}
