/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetadataDecoder;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Component
public class SqlMetaDataMapper implements RowMapper<List<SqlMetaDataBo>> {

    private final static byte[] SQL_METADATA_CQ = HbaseColumnFamily.SQL_METADATA_VER2_SQL.QUALIFIER_SQLSTATEMENT;

    private final RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    private final RowKeyDecoder<MetaDataRowKey> decoder = new MetadataDecoder();

    public SqlMetaDataMapper(@Qualifier("metadataRowKeyDistributor2") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.rowKeyDistributorByHashPrefix = Objects.requireNonNull(rowKeyDistributorByHashPrefix, "rowKeyDistributorByHashPrefix");
    }

    @Override
    public List<SqlMetaDataBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] rowKey = getOriginalKey(result.getRow());

        final MetaDataRowKey key = decoder.decodeRowKey(rowKey);

        List<SqlMetaDataBo> sqlMetaDataList = new ArrayList<>(result.size());

        for (Cell cell : result.rawCells()) {
            String sql = readSql(cell);

            SqlMetaDataBo sqlMetaDataBo = new SqlMetaDataBo(key.getAgentId(), key.getAgentStartTime(), key.getId(), sql);
            sqlMetaDataList.add(sqlMetaDataBo);
        }
        return sqlMetaDataList;
    }

    private String readSql(Cell cell) {
        if (CellUtil.matchingQualifier(cell, SQL_METADATA_CQ)) {
            return CellUtils.valueToString(cell);
        } else {
            // backward compatibility
            return CellUtils.qualifierToString(cell);
        }
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }

}
