/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.util.CellUtils;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetadataDecoder;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SqlUidMetaDataMapper implements RowMapper<List<SqlUidMetaDataBo>> {
    private final static byte[] SQL_UID_METADATA_CF_SQL_QUALI_SQLSTATEMENT = HbaseTables.SQL_UID_METADATA_SQL.QUALIFIER_SQLSTATEMENT;

    private final RowKeyDecoder<UidMetaDataRowKey> decoder = new UidMetadataDecoder();

    public SqlUidMetaDataMapper() {
    }

    @Override
    public List<SqlUidMetaDataBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] rowKey = result.getRow();

        final UidMetaDataRowKey key = decoder.decodeRowKey(rowKey);

        List<SqlUidMetaDataBo> sqlUidMetaDataList = new ArrayList<>();

        for (Cell cell : result.rawCells()) {
            if (CellUtil.matchingQualifier(cell, SQL_UID_METADATA_CF_SQL_QUALI_SQLSTATEMENT)) {
                String sqlStatement = CellUtils.valueToString(cell);
                SqlUidMetaDataBo sqlUidMetaDataBo = new SqlUidMetaDataBo(key.getAgentId(), key.getAgentStartTime(), key.getUid(), sqlStatement);
                sqlUidMetaDataList.add(sqlUidMetaDataBo);
            }
        }
        return sqlUidMetaDataList;
    }

}
