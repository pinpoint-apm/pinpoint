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
public class TableConfiguration {

    public static final TableConfiguration EMPTY_CONFIGURATION = new TableConfiguration() {
        @Override
        public String toString() {
            return "NONE";
        }
    };

    private Long maxFileSize;
    private Boolean readOnly;
    private Boolean compactionEnabled;
    private Long memstoreFlushSize;
    private Durability durability;

    public enum Durability {
        ASYNC_WAL,
        FSYNC_WAL,
        SKIP_WAL,
        SYNC_WAL,
        USE_DEFAULT
    }

    private TableConfiguration() {
        this.maxFileSize = null;
        this.readOnly = null;
        this.compactionEnabled = null;
        this.memstoreFlushSize = null;
        this.durability = null;
    }

    private TableConfiguration(Builder builder) {
        this.maxFileSize = builder.maxFileSize;
        this.readOnly = builder.readOnly;
        this.compactionEnabled = builder.compactionEnabled;
        this.memstoreFlushSize = builder.memstoreFlushSize;
        this.durability = builder.durability;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public Boolean getCompactionEnabled() {
        return compactionEnabled;
    }

    public Long getMemstoreFlushSize() {
        return memstoreFlushSize;
    }

    public Durability getDurability() {
        return durability;
    }

    public static class Builder {

        private Long maxFileSize;
        private Boolean readOnly;
        private Boolean compactionEnabled;
        private Long memstoreFlushSize;
        private Durability durability;

        public Builder maxFileSize(Long maxFileSize) {
            this.maxFileSize = maxFileSize;
            return this;
        }

        public Builder readOnly(Boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public Builder compactionEnabled(Boolean compactionEnabled) {
            this.compactionEnabled = compactionEnabled;
            return this;
        }

        public Builder memstoreFlushSize(Long memstoreFlushSize) {
            this.memstoreFlushSize = memstoreFlushSize;
            return this;
        }

        public Builder durability(Durability durability) {
            this.durability = durability;
            return this;
        }

        public TableConfiguration build() {
            return new TableConfiguration(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableConfiguration that = (TableConfiguration) o;
        return Objects.equals(maxFileSize, that.maxFileSize) &&
                Objects.equals(readOnly, that.readOnly) &&
                Objects.equals(compactionEnabled, that.compactionEnabled) &&
                Objects.equals(memstoreFlushSize, that.memstoreFlushSize) &&
                durability == that.durability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxFileSize, readOnly, compactionEnabled, memstoreFlushSize, durability);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TableConfiguration{");
        sb.append("maxFileSize=").append(maxFileSize);
        sb.append(", readOnly=").append(readOnly);
        sb.append(", compactionEnabled=").append(compactionEnabled);
        sb.append(", memstoreFlushSize=").append(memstoreFlushSize);
        sb.append(", durability=").append(durability);
        sb.append('}');
        return sb.toString();
    }
}
