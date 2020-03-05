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

import com.navercorp.pinpoint.common.hbase.HbaseAdminOperation;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class CreateTableCommand extends TableCommand {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final byte[][] splitKeys;

    CreateTableCommand(TableName tableName, Compression.Algorithm compressionAlgorithm, byte[][] splitKeys) {
        super(new HTableDescriptor(Objects.requireNonNull(tableName, "tableName")), compressionAlgorithm);
        this.splitKeys = Objects.requireNonNull(splitKeys, "splitKeys");
    }

    @Override
    public boolean execute(HbaseAdminOperation hbaseAdminOperation) {
        HTableDescriptor htd = getHtd();
        TableName tableName = htd.getTableName();
        if (hbaseAdminOperation.tableExists(tableName)) {
            return false;
        }
        logger.info("Creating {} table...", tableName);
        if (ArrayUtils.isEmpty(splitKeys)) {
            hbaseAdminOperation.createTable(htd);
            logger.info("{} table created.", tableName);
        } else {
            hbaseAdminOperation.createTable(htd, splitKeys);
            logger.info("{} table created with {} splitKeys.", tableName, splitKeys.length + 1);
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateTableCommand{");
        sb.append("htd=").append(getHtd());
        sb.append("compressionAlgorithm=").append(getCompressionAlgorithm().getName());
        sb.append(", splitKeys=").append(Arrays.toString(splitKeys));
        sb.append('}');
        return sb.toString();
    }
}
