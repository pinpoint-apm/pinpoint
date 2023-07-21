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
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This command is solely for adding new column families to an existing table.
 * Modification of table and column family configuration are not supported.
 *
 * @author HyunGil Jeong
 */
public class ModifyTableCommand extends TableCommand {

    private final Logger logger = LogManager.getLogger(this.getClass());

    ModifyTableCommand(TableDescriptor htd, Compression.Algorithm compressionAlgorithm) {
        super(htd, compressionAlgorithm);
    }

    @Override
    public boolean execute(HbaseAdminOperation hbaseAdminOperation) {
        TableDescriptor htd = buildDescriptor();
        ColumnFamilyDescriptor[] cfDescriptors = htd.getColumnFamilies();

        TableName tableName = htd.getTableName();
        TableDescriptor currentHtd = hbaseAdminOperation.getTableDescriptor(tableName);

        // Filter existing column families as column family modification is not supported.
        // We could use modifyTable(TableDescriptor) to add column families, but this deletes existing column families
        // if they are not specified in TableDescriptor and this may be dangerous.
        // Instead, use addColumn.
        boolean changesMade = false;
        for (ColumnFamilyDescriptor cf : cfDescriptors) {
            if (!currentHtd.hasColumnFamily(cf.getName())) {
                logger.info("Adding {} to {} table.", cf, tableName);
                hbaseAdminOperation.addColumn(tableName, cf);
                changesMade = true;
            }
        }
        return changesMade;
    }

    @Override
    public String toString() {
        return "ModifyTableCommand{TableDesc=" + buildDescriptor() +
                ", compressionAlgorithm=" + getCompressionAlgorithm() +
                '}';
    }
}
