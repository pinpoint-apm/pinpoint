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

package com.navercorp.pinpoint.hbase.schema.reader.core;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ColumnFamilyConfiguration {

    public static final ColumnFamilyConfiguration EMPTY_CONFIGURATION = new ColumnFamilyConfiguration() {
        @Override
        public String toString() {
            return "NONE";
        }
    };

    private final Boolean blockCacheEnabled;
    private final Integer replicationScope;
    private final Boolean inMemory;
    private final Integer timeToLive;
    private final DataBlockEncoding dataBlockEncoding;
    private final Integer blockSize;
    private final Integer maxVersions;
    private final Integer minVersions;
    private final BloomFilter bloomFilter;

    public enum DataBlockEncoding {
        NONE,
        PREFIX,
        DIFF,
        FAST_DIFF,
        PREFIX_TREE
    }

    public enum BloomFilter {
        NONE,
        ROW,
        ROWCOL
    }

    private ColumnFamilyConfiguration() {
        this.blockCacheEnabled = null;
        this.replicationScope = null;
        this.inMemory = null;
        this.timeToLive = null;
        this.dataBlockEncoding = null;
        this.blockSize = null;
        this.maxVersions = null;
        this.minVersions = null;
        this.bloomFilter = null;
    }

    private ColumnFamilyConfiguration(Builder builder) {
        this.blockCacheEnabled = builder.blockCacheEnabled;
        this.replicationScope = builder.replicationScope;
        this.inMemory = builder.inMemory;
        this.timeToLive = builder.timeToLive;
        this.dataBlockEncoding = builder.dataBlockEncoding;
        this.blockSize = builder.blockSize;
        this.maxVersions = builder.maxVersions;
        this.minVersions = builder.minVersions;
        this.bloomFilter = builder.bloomFilter;
    }

    public Boolean getBlockCacheEnabled() {
        return blockCacheEnabled;
    }

    public Integer getReplicationScope() {
        return replicationScope;
    }

    public Boolean getInMemory() {
        return inMemory;
    }

    public Integer getTimeToLive() {
        return timeToLive;
    }

    public DataBlockEncoding getDataBlockEncoding() {
        return dataBlockEncoding;
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public Integer getMaxVersions() {
        return maxVersions;
    }

    public Integer getMinVersions() {
        return minVersions;
    }

    public BloomFilter getBloomFilter() {
        return bloomFilter;
    }

    public static class Builder {

        private Boolean blockCacheEnabled;
        private Integer replicationScope;
        private Boolean inMemory;
        private Integer timeToLive;
        private DataBlockEncoding dataBlockEncoding;
        private Integer blockSize;
        private Integer maxVersions;
        private Integer minVersions;
        private BloomFilter bloomFilter;

        public Builder blockCacheEnabled(Boolean blockCacheEnabled) {
            this.blockCacheEnabled = blockCacheEnabled;
            return this;
        }

        public Builder replicationScope(Integer replicationScope) {
            this.replicationScope = replicationScope;
            return this;
        }

        public Builder inMemory(Boolean inMemory) {
            this.inMemory = inMemory;
            return this;
        }

        public Builder timeToLive(Integer timeToLive) {
            this.timeToLive = timeToLive;
            return this;
        }

        public Builder dataBlockEncoding(DataBlockEncoding dataBlockEncoding) {
            this.dataBlockEncoding = dataBlockEncoding;
            return this;
        }

        public Builder blockSize(Integer blockSize) {
            this.blockSize = blockSize;
            return this;
        }

        public Builder maxVersions(Integer maxVersions) {
            this.maxVersions = maxVersions;
            return this;
        }

        public Builder minVersions(Integer minVersions) {
            this.minVersions = minVersions;
            return this;
        }

        public Builder bloomFilter(BloomFilter bloomFilter) {
            this.bloomFilter = bloomFilter;
            return this;
        }

        public ColumnFamilyConfiguration build() {
            return new ColumnFamilyConfiguration(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnFamilyConfiguration that = (ColumnFamilyConfiguration) o;
        return Objects.equals(blockCacheEnabled, that.blockCacheEnabled) &&
                Objects.equals(replicationScope, that.replicationScope) &&
                Objects.equals(inMemory, that.inMemory) &&
                Objects.equals(timeToLive, that.timeToLive) &&
                dataBlockEncoding == that.dataBlockEncoding &&
                Objects.equals(blockSize, that.blockSize) &&
                Objects.equals(maxVersions, that.maxVersions) &&
                Objects.equals(minVersions, that.minVersions) &&
                bloomFilter == that.bloomFilter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockCacheEnabled, replicationScope, inMemory, timeToLive, dataBlockEncoding, blockSize, maxVersions, minVersions, bloomFilter);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ColumnFamilyConfiguration{");
        sb.append("blockCacheEnabled=").append(blockCacheEnabled);
        sb.append(", replicationScope=").append(replicationScope);
        sb.append(", inMemory=").append(inMemory);
        sb.append(", timeToLive=").append(timeToLive);
        sb.append(", dataBlockEncoding=").append(dataBlockEncoding);
        sb.append(", blockSize=").append(blockSize);
        sb.append(", maxVersions=").append(maxVersions);
        sb.append(", minVersions=").append(minVersions);
        sb.append(", bloomFilter=").append(bloomFilter);
        sb.append('}');
        return sb.toString();
    }
}
