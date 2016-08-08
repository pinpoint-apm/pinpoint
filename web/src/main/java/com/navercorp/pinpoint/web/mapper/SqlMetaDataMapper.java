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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.RowMapper;

/**
 * @author emeroad
 * @author minwoo.jung
 */
//@Component
public class SqlMetaDataMapper implements RowMapper<List<SqlMetaDataBo>> {

//    @Autowired
//    @Qualifier("metadataRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;
    
    private final static String SQL_METADATA_CF_SQL_QUALI_SQLSTATEMENT = Bytes.toString(HBaseTables.SQL_METADATA_VER2_CF_SQL_QUALI_SQLSTATEMENT);

    @Override
    public List<SqlMetaDataBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] rowKey = getOriginalKey(result.getRow());

        List<SqlMetaDataBo> sqlMetaDataList = new ArrayList<>();
        Cell[] rawCell = result.rawCells();
        for (Cell cell : rawCell) {
            SqlMetaDataBo sqlMetaDataBo = new SqlMetaDataBo();
            sqlMetaDataBo.readRowKey(rowKey);
            String sql = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());

            if (SQL_METADATA_CF_SQL_QUALI_SQLSTATEMENT.equals(sql)) {
                sql = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            }

            sqlMetaDataBo.setSql(sql);
            sqlMetaDataList.add(sqlMetaDataBo);
        }
        return sqlMetaDataList;
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }
    
    public void setRowKeyDistributorByHashPrefix(RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.rowKeyDistributorByHashPrefix = rowKeyDistributorByHashPrefix;
    }
}
