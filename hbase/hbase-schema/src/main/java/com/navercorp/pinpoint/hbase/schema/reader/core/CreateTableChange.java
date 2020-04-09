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

import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class CreateTableChange extends TableChange {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private final SplitOption splitOption;

    public CreateTableChange(
            String name,
            TableConfiguration tableConfiguration,
            List<ColumnFamilyChange> columnFamilyChanges,
            SplitOption splitOption) {
        super(name, tableConfiguration, columnFamilyChanges);
        this.splitOption = Objects.requireNonNull(splitOption, "splitOption");
    }

    public interface SplitOption {

        byte[][] getSplitKeys();

        public SplitOption NONE = new SplitOption() {
            @Override
            public byte[][] getSplitKeys() {
                return new byte[0][];
            }

            @Override
            public String toString() {
                return "NONE";
            }
        };

        public class Manual implements SplitOption {
            private final List<String> splitKeys;

            public Manual(List<String> splitKeys) {
                if (CollectionUtils.isEmpty(splitKeys)) {
                    throw new IllegalArgumentException("splitKeys must not be empty");
                }
                this.splitKeys = splitKeys;
            }

            @Override
            public byte[][] getSplitKeys() {
                byte[][] splits = new byte[splitKeys.size()][];
                for (int i = 0; i < splitKeys.size(); i++) {
                    splits[i] = Bytes.toBytesBinary(splitKeys.get(i));
                }
                return splits;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Manual manual = (Manual) o;
                return Objects.equals(splitKeys, manual.splitKeys);
            }

            @Override
            public int hashCode() {
                return Objects.hash(splitKeys);
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("Manual{");
                sb.append("splitKeys=").append(splitKeys);
                sb.append('}');
                return sb.toString();
            }
        }

        // Implementation copied from hbase's RegionSplitter$UniformSplit
        // https://github.com/apache/hbase/blob/master/hbase-server/src/main/java/org/apache/hadoop/hbase/util/RegionSplitter.java
        public class Auto implements SplitOption {
            private static final byte xFF = (byte) 0xFF;
            private static final byte[] FIRST_ROW_BYTES = EMPTY_BYTE_ARRAY;
            private static final byte[] LAST_ROW_BYTES = new byte[]{xFF, xFF, xFF, xFF, xFF, xFF, xFF, xFF};

            private final int numRegions;

            public Auto(int numRegions) {
                if (!(numRegions > 1)) {
                    throw new IllegalArgumentException("numRegions must be greater than 1");
                }
                this.numRegions = numRegions;

            }

            @Override
            public byte[][] getSplitKeys() {
                return generateSplitKeys();
            }

            private byte[][] generateSplitKeys() {
                byte[][] splits = Bytes.split(FIRST_ROW_BYTES, LAST_ROW_BYTES, true, numRegions - 1);
                // remove endpoints, which are included in the splits list
                if (splits == null) {
                    throw new IllegalStateException("Could not generate split keys, numRegions : " + numRegions);
                }
                return Arrays.copyOfRange(splits, 1, splits.length - 1);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Auto auto = (Auto) o;
                return numRegions == auto.numRegions;
            }

            @Override
            public int hashCode() {
                return Objects.hash(numRegions);
            }

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("Auto{");
                sb.append("numRegions=").append(numRegions);
                sb.append('}');
                return sb.toString();
            }
        }
    }

    @Override
    public ChangeType getType() {
        return ChangeType.CREATE;
    }

    @Override
    public byte[][] getSplitKeys() {
        return splitOption.getSplitKeys();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CreateTableChange that = (CreateTableChange) o;
        return Objects.equals(splitOption, that.splitOption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), splitOption);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateTableChange{");
        sb.append("name='").append(getName()).append('\'');
        sb.append(", columnFamilyChanges=").append(getColumnFamilyChanges());
        sb.append(", configuration=").append(getTableConfiguration());
        sb.append(", splitOption=").append(splitOption);
        sb.append('}');
        return sb.toString();
    }
}
