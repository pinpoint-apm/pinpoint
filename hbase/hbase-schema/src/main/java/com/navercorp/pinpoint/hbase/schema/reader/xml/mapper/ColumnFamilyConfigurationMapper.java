/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.hbase.schema.reader.xml.mapper;

import com.navercorp.pinpoint.hbase.schema.definition.xml.BloomFilter;
import com.navercorp.pinpoint.hbase.schema.definition.xml.DataBlockEncoding;
import com.navercorp.pinpoint.hbase.schema.definition.xml.Table;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyConfiguration;

/**
 * @author HyunGil Jeong
 */
public class ColumnFamilyConfigurationMapper {

    public ColumnFamilyConfiguration mapConfiguration(Table.CreateColumnFamily.Configuration configuration) {
        if (configuration == null) {
            return ColumnFamilyConfiguration.EMPTY_CONFIGURATION;
        }
        return new ColumnFamilyConfiguration.Builder()
                .blockCacheEnabled(configuration.isBlockCacheEnabled())
                .replicationScope(configuration.getReplicationScope())
                .inMemory(configuration.isInMemory())
                .timeToLive(configuration.getTimeToLive())
                .dataBlockEncoding(mapDataBlockEncoding(configuration.getDataBlockEncoding()))
                .blockSize(configuration.getBlockSize())
                .maxVersions(configuration.getMaxVersions())
                .minVersions(configuration.getMinVersions())
                .bloomFilter(mapBloomFilter(configuration.getBloomFilter()))
                .build();
    }

    private ColumnFamilyConfiguration.DataBlockEncoding mapDataBlockEncoding(DataBlockEncoding dataBlockEncoding) {
        if (dataBlockEncoding == null) {
            return null;
        }
        return ColumnFamilyConfiguration.DataBlockEncoding.valueOf(dataBlockEncoding.value());
    }

    private ColumnFamilyConfiguration.BloomFilter mapBloomFilter(BloomFilter bloomFilter) {
        if (bloomFilter == null) {
            return null;
        }
        return ColumnFamilyConfiguration.BloomFilter.valueOf(bloomFilter.value());
    }
}
