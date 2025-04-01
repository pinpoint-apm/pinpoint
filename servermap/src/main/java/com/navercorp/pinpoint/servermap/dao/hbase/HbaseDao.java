/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.servermap.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.servermap.bo.CallCount;
import com.navercorp.pinpoint.servermap.bo.DirectionalBo;
import com.navercorp.pinpoint.servermap.dao.hbase.statistics.ApplicationMapColumnName;
import com.navercorp.pinpoint.servermap.dao.hbase.statistics.ApplicationMapRowKey;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class HbaseDao {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HbaseColumnFamily descriptor;
    private final HbaseOperations hbaseTemplate;
    private final TableNameProvider tableNameProvider;
    private final HbaseBatchWriter hbaseBatchWriter;
    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;


    public HbaseDao(
            HbaseColumnFamily descriptor,
            HbaseOperations hbaseTemplate,
            TableNameProvider tableNameProvider,
            HbaseBatchWriter hbaseBatchWriter,
            RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix
    ) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.hbaseBatchWriter = Objects.requireNonNull(hbaseBatchWriter, "hbaseBatchWriter");
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }


    public void insert(List<DirectionalBo> directionalBoList) {
        Objects.requireNonNull(directionalBoList, "directionalBoList");
        for (DirectionalBo directionalBo : directionalBoList) {
            insert(directionalBo);
        }
    }



    public void insert(DirectionalBo directionalBo) {
        Objects.requireNonNull(directionalBo, "directionalBo");
        if (!Objects.equals(directionalBo.getTableName().getName(), descriptor.getTable().getName())) {
            return;
        }

        logger.info("insert application map data: {}", directionalBo);

        ArrayList<Put> puts = new ArrayList<>();

        for (CallCount callCount : directionalBo.getCallCountList()) {

            ApplicationMapRowKey rowKey = new ApplicationMapRowKey(
                    directionalBo.getMainServiceId(),
                    directionalBo.getMainServiceType(),
                    directionalBo.getMainApplicationName(),
                    callCount.timestamp()
            );

            ApplicationMapColumnName columnName = new ApplicationMapColumnName(
                    directionalBo.getSubServiceId(),
                    directionalBo.getSubServiceType(),
                    directionalBo.getSubApplicationName(),
                    directionalBo.getSlotNumber()
            );

            Put put = new Put(getRowKey(rowKey));
            put.addColumn(
                    descriptor.getName(),
                    columnName.getColumnName(),
                    Bytes.toBytes(callCount.callCount())
            );
            puts.add(put);
        }

        TableName applicationMapTableName = tableNameProvider.getTableName(descriptor.getTable());
        this.hbaseTemplate.put(applicationMapTableName, puts);

        // Maybe later
        // this.hbaseBatchWriter.put(applicationMapTableName, puts);

    }

    private byte[] getRowKey(ApplicationMapRowKey rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey.getRowKey());
    }

}
