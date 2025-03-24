/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class HbaseColumnFamily {
    private final HbaseTable hBaseTable;
    private final byte[] columnFamilyName;

    HbaseColumnFamily(HbaseTable hBaseTable, byte[] columnFamilyName) {
        this.hBaseTable = Objects.requireNonNull(hBaseTable, "hBaseTable");
        Assert.isTrue(ArrayUtils.hasLength(columnFamilyName), "columnFamilyName must not be empty");
        this.columnFamilyName = columnFamilyName;
    }

    public HbaseTable getTable() {
        return hBaseTable;
    }

    public byte[] getName() {
        return columnFamilyName;
    }
}
