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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.hbase.schema.reader.core.ChangeType;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyChange;
import com.navercorp.pinpoint.hbase.schema.reader.core.ColumnFamilyConfiguration;
import com.navercorp.pinpoint.hbase.schema.reader.core.TableConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public abstract class TableCommand implements HbaseSchemaCommand {

    private final HTableDescriptor htd;
    private final Compression.Algorithm compressionAlgorithm;

    TableCommand(HTableDescriptor htd, Compression.Algorithm compressionAlgorithm) {
        this.htd = Objects.requireNonNull(htd, "htd");
        this.compressionAlgorithm = Objects.requireNonNull(compressionAlgorithm, "compressionAlgorithm");

    }

    public TableName getTableName() {
        return htd.getTableName();
    }

    protected final HTableDescriptor getHtd() {
        return htd;
    }

    protected final Compression.Algorithm getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    void applyConfiguration(TableConfiguration tableConfiguration) {
        Long maxFileSize = tableConfiguration.getMaxFileSize();
        if (maxFileSize != null) {
            htd.setMaxFileSize(maxFileSize);
        }
        Boolean readOnly = tableConfiguration.getReadOnly();
        if (readOnly != null) {
            htd.setReadOnly(readOnly);
        }
        Boolean compactionEnabled = tableConfiguration.getCompactionEnabled();
        if (compactionEnabled != null) {
            htd.setCompactionEnabled(compactionEnabled);
        }
        Long memstoreFlushSize = tableConfiguration.getMemstoreFlushSize();
        if (memstoreFlushSize != null) {
            htd.setMemStoreFlushSize(memstoreFlushSize);
        }
        TableConfiguration.Durability durability = tableConfiguration.getDurability();
        if (durability != null) {
            htd.setDurability(Durability.valueOf(durability.name()));
        }
    }

    void applyColumnFamilyChanges(List<ColumnFamilyChange> columnFamilyChanges) {
        if (CollectionUtils.isEmpty(columnFamilyChanges)) {
            return;
        }
        for (ColumnFamilyChange columnFamilyChange : columnFamilyChanges) {
            ChangeType changeType = columnFamilyChange.getType();
            if (changeType == ChangeType.CREATE) {
                HColumnDescriptor family = newColumnDescriptor(columnFamilyChange);
                if (htd.hasFamily(family.getName())) {
                    throw new IllegalArgumentException("Cannot add an existing column family : " + htd.getNameAsString());
                }
                htd.addFamily(family);
            } else {
                throw new UnsupportedOperationException("Unknown change type : " + changeType);
            }
        }
    }

    private HColumnDescriptor newColumnDescriptor(ColumnFamilyChange columnFamilyChange) {
        HColumnDescriptor hcd = new HColumnDescriptor(columnFamilyChange.getName());
        ColumnFamilyConfiguration columnFamilyConfiguration = columnFamilyChange.getColumnFamilyConfiguration();
        Boolean blockCacheEnabled = columnFamilyConfiguration.getBlockCacheEnabled();
        if (blockCacheEnabled != null) {
            hcd.setBlockCacheEnabled(blockCacheEnabled);
        }
        Integer replicationScope = columnFamilyConfiguration.getReplicationScope();
        if (replicationScope != null) {
            hcd.setScope(replicationScope);
        }
        Boolean inMemory = columnFamilyConfiguration.getInMemory();
        if (inMemory != null) {
            hcd.setInMemory(inMemory);
        }
        Integer timeToLive = columnFamilyConfiguration.getTimeToLive();
        if (timeToLive != null) {
            hcd.setTimeToLive(timeToLive);
        }
        ColumnFamilyConfiguration.DataBlockEncoding dataBlockEncoding =
                columnFamilyConfiguration.getDataBlockEncoding();
        if (dataBlockEncoding != null) {
            hcd.setDataBlockEncoding(DataBlockEncoding.valueOf(dataBlockEncoding.name()));
        }
        Integer blockSize = columnFamilyConfiguration.getBlockSize();
        if (blockSize != null) {
            hcd.setBlocksize(blockSize);
        }
        Integer maxVersions = columnFamilyConfiguration.getMaxVersions();
        if (maxVersions != null) {
            hcd.setMaxVersions(maxVersions);
        }
        Integer minVersions = columnFamilyConfiguration.getMinVersions();
        if (minVersions != null) {
            hcd.setMinVersions(minVersions);
        }
        ColumnFamilyConfiguration.BloomFilter bloomFilter = columnFamilyConfiguration.getBloomFilter();
        if (bloomFilter != null) {
            hcd.setBloomFilterType(BloomType.valueOf(bloomFilter.name()));
        }
        if (compressionAlgorithm != Compression.Algorithm.NONE) {
            hcd.setCompressionType(compressionAlgorithm);
        }
        return hcd;
    }
}
