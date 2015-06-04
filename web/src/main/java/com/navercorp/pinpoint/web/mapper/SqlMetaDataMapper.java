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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.common.bo.SqlMetaDataBo;

/**
 * @author emeroad
 */
@Component
public class SqlMetaDataMapper implements RowMapper<List<SqlMetaDataBo>> {

    @Autowired
    @Qualifier("metadataRowKeyDistributor")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    @Override
    public List<SqlMetaDataBo> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        final byte[] rowKey = getOriginalKey(result.getRow());

        List<SqlMetaDataBo> sqlMetaDataList = new ArrayList<SqlMetaDataBo>();
        Cell[] rawCell = result.rawCells();
        for (Cell cell : rawCell) {
            SqlMetaDataBo sqlMetaDataBo = new SqlMetaDataBo();
            sqlMetaDataBo.readRowKey(rowKey);
            String sql = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
            sqlMetaDataBo.setSql(sql);
            sqlMetaDataList.add(sqlMetaDataBo);
        }
        return sqlMetaDataList;
    }

    private byte[] getOriginalKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getOriginalKey(rowKey);
    }
}
