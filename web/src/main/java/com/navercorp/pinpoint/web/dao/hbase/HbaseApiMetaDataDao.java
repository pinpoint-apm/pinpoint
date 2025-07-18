/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.DefaultMetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.web.cache.CacheConfiguration;
import com.navercorp.pinpoint.web.dao.ApiMetaDataDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author jaehong.kim
 */
@Repository
public class HbaseApiMetaDataDao implements ApiMetaDataDao {
    static final String SPEL_KEY = "#agentId.toString() + '.' + #time.toString() + '.' + #apiId.toString()";

    private static final HbaseTables.ApiMetadata DESCRIPTOR = HbaseTables.API_METADATA_API;

    private final HbaseOperations hbaseOperations;
    private final TableNameProvider tableNameProvider;

    private final RowMapper<List<ApiMetaDataBo>> apiMetaDataMapper;

    private final RowKeyEncoder<MetaDataRowKey> rowKeyEncoder;

    public HbaseApiMetaDataDao(HbaseOperations hbaseOperations,
                               @Qualifier("metaDataRowKeyEncoder")
                               RowKeyEncoder<MetaDataRowKey> rowKeyEncoder,
                               TableNameProvider tableNameProvider,
                               @Qualifier("apiMetaDataMapper")
                               RowMapper<List<ApiMetaDataBo>> apiMetaDataMapper) {
        this.hbaseOperations = Objects.requireNonNull(hbaseOperations, "hbaseOperations");
        this.rowKeyEncoder = Objects.requireNonNull(rowKeyEncoder, "rowKeyEncoder");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.apiMetaDataMapper = Objects.requireNonNull(apiMetaDataMapper, "apiMetaDataMapper");
    }

    @Override
    @Cacheable(cacheNames="apiMetaData", key=SPEL_KEY, cacheManager = CacheConfiguration.API_METADATA_CACHE_NAME)
    public List<ApiMetaDataBo> getApiMetaData(String agentId, long time, int apiId) {
        Objects.requireNonNull(agentId, "agentId");

        MetaDataRowKey metaDataRowKey = new DefaultMetaDataRowKey(agentId, time, apiId);
        byte[] sqlId = rowKeyEncoder.encodeRowKey(metaDataRowKey);

        Get get = new Get(sqlId);
        get.addFamily(DESCRIPTOR.getName());

        TableName apiMetaDataTableName = tableNameProvider.getTableName(DESCRIPTOR.getTable());
        return hbaseOperations.get(apiMetaDataTableName, get, apiMetaDataMapper);
    }

}
