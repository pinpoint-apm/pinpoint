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

package com.navercorp.pinpoint.hbase.schema.core.command;

import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeType;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyConfiguration;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public abstract class TableCommand implements HbaseSchemaCommand {

    protected final TableDescriptorBuilder builder;
    private final Compression.Algorithm compressionAlgorithm;

    TableCommand(TableDescriptor tableDescriptor, Compression.Algorithm compressionAlgorithm) {
        Objects.requireNonNull(tableDescriptor, "tableDescriptor");
        this.builder = TableDescriptorBuilder.newBuilder(tableDescriptor);
        this.compressionAlgorithm = Objects.requireNonNull(compressionAlgorithm, "compressionAlgorithm");
    }

    TableCommand(TableName tableName, Compression.Algorithm compressionAlgorithm) {
        Objects.requireNonNull(tableName, "tableName");
        this.builder = TableDescriptorBuilder.newBuilder(tableName);
        this.compressionAlgorithm = Objects.requireNonNull(compressionAlgorithm, "compressionAlgorithm");
    }

    protected final TableDescriptor buildDescriptor() {
        return builder.build();
    }

    protected final Compression.Algorithm getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    void applyConfiguration(TableConfiguration tableConfiguration) {
        Long maxFileSize = tableConfiguration.getMaxFileSize();
        if (maxFileSize != null) {
            builder.setMaxFileSize(maxFileSize);
        }
        Boolean readOnly = tableConfiguration.getReadOnly();
        if (readOnly != null) {
            builder.setReadOnly(readOnly);
        }
        Boolean compactionEnabled = tableConfiguration.getCompactionEnabled();
        if (compactionEnabled != null) {
            builder.setCompactionEnabled(compactionEnabled);
        }
        Long memstoreFlushSize = tableConfiguration.getMemstoreFlushSize();
        if (memstoreFlushSize != null) {
            builder.setMemStoreFlushSize(memstoreFlushSize);
        }
        TableConfiguration.Durability durability = tableConfiguration.getDurability();
        if (durability != null) {
            builder.setDurability(Durability.valueOf(durability.name()));
        }
    }

    void applyColumnFamilyChanges(List<ColumnFamilyChange> columnFamilyChanges) {
        if (CollectionUtils.isEmpty(columnFamilyChanges)) {
            return;
        }
        TableDescriptor tableDescriptor = builder.build();

        for (ColumnFamilyChange columnFamilyChange : columnFamilyChanges) {
            ChangeType changeType = columnFamilyChange.getType();
            if (changeType == ChangeType.CREATE) {
                ColumnFamilyDescriptor family = newColumnDescriptor(columnFamilyChange);
                if (tableDescriptor.hasColumnFamily(family.getName())) {
                    throw new IllegalArgumentException("Cannot add an existing column family : " + tableDescriptor.getTableName().getNameAsString());
                }
                builder.setColumnFamily(family);
            } else {
                throw new UnsupportedOperationException("Unknown change type : " + changeType);
            }
        }
    }

    private ColumnFamilyDescriptor newColumnDescriptor(ColumnFamilyChange columnFamilyChange) {
        byte[] name = BytesUtils.toBytes(columnFamilyChange.getName());
        ColumnFamilyDescriptorBuilder builder = ColumnFamilyDescriptorBuilder.newBuilder(name);

        ColumnFamilyConfiguration columnFamilyConfiguration = columnFamilyChange.getColumnFamilyConfiguration();
        Boolean blockCacheEnabled = columnFamilyConfiguration.getBlockCacheEnabled();
        if (blockCacheEnabled != null) {
            builder.setBlockCacheEnabled(blockCacheEnabled);
        }
        Integer replicationScope = columnFamilyConfiguration.getReplicationScope();
        if (replicationScope != null) {
            builder.setScope(replicationScope);
        }
        Boolean inMemory = columnFamilyConfiguration.getInMemory();
        if (inMemory != null) {
            builder.setInMemory(inMemory);
        }
        Integer timeToLive = columnFamilyConfiguration.getTimeToLive();
        if (timeToLive != null) {
            builder.setTimeToLive(timeToLive);
        }
        ColumnFamilyConfiguration.DataBlockEncoding dataBlockEncoding =
                columnFamilyConfiguration.getDataBlockEncoding();
        if (dataBlockEncoding != null) {
            builder.setDataBlockEncoding(DataBlockEncoding.valueOf(dataBlockEncoding.name()));
        }
        Integer blockSize = columnFamilyConfiguration.getBlockSize();
        if (blockSize != null) {
            builder.setBlocksize(blockSize);
        }
        Integer maxVersions = columnFamilyConfiguration.getMaxVersions();
        if (maxVersions != null) {
            builder.setMaxVersions(maxVersions);
        }
        Integer minVersions = columnFamilyConfiguration.getMinVersions();
        if (minVersions != null) {
            builder.setMinVersions(minVersions);
        }
        ColumnFamilyConfiguration.BloomFilter bloomFilter = columnFamilyConfiguration.getBloomFilter();
        if (bloomFilter != null) {
            builder.setBloomFilterType(BloomType.valueOf(bloomFilter.name()));
        }
        if (compressionAlgorithm != Compression.Algorithm.NONE) {
            builder.setCompressionType(compressionAlgorithm);
        }
        return builder.build();
    }
}
