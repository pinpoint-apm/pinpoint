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
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command is solely for adding new column families to an existing table.
 * Modification of table and column family configuration are not supported.
 *
 * @author HyunGil Jeong
 */
public class ModifyTableCommand extends TableCommand {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    ModifyTableCommand(HTableDescriptor htd, Compression.Algorithm compressionAlgorithm) {
        super(htd, compressionAlgorithm);
    }

    @Override
    public boolean execute(HbaseAdminOperation hbaseAdminOperation) {
        HTableDescriptor htd = getHtd();
        HColumnDescriptor[] hcds = htd.getColumnFamilies();
        if (ArrayUtils.isEmpty(hcds)) {
            return false;
        }

        TableName tableName = htd.getTableName();
        HTableDescriptor currentHtd = hbaseAdminOperation.getTableDescriptor(tableName);

        // Filter existing column families as column family modification is not supported.
        // We could use modifyTable(HTableDescriptor) to add column families, but this deletes existing column families
        // if they are not specified in HTableDescriptor and this may be dangerous.
        // Instead, use addColumn.
        boolean changesMade = false;
        for (HColumnDescriptor hcd : hcds) {
            if (!currentHtd.hasFamily(hcd.getName())) {
                logger.info("Adding {} to {} table.", hcd, tableName);
                hbaseAdminOperation.addColumn(tableName, hcd);
                changesMade = true;
            }
        }
        return changesMade;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModifyTableCommand{");
        sb.append("htd=").append(getHtd());
        sb.append(", compressionAlgorithm=").append(getCompressionAlgorithm());
        sb.append('}');
        return sb.toString();
    }
}
