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

package com.navercorp.pinpoint.web.dao.hbase;

import java.util.List;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;

import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.web.dao.SqlMetaDataDao;

/**
 * @author emeroad
 * @author minwoo.jung
 */
//@Repository
public class HbaseSqlMetaDataDao implements SqlMetaDataDao {

    @Autowired
    private HbaseOperations2 hbaseOperations2;

//    @Autowired
//    @Qualifier("sqlMetaDataMapper")
    private RowMapper<List<SqlMetaDataBo>> sqlMetaDataMapper;

//    @Autowired
//    @Qualifier("metadataRowKeyDistributor2")
    private RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix;

    @Override
    public List<SqlMetaDataBo> getSqlMetaData(String agentId, long time, int sqlId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        SqlMetaDataBo sqlMetaData = new SqlMetaDataBo(agentId, time, sqlId);
        byte[] rowKey = getDistributedKey(sqlMetaData.toRowKey());

        Get get = new Get(rowKey);
        get.addFamily(HBaseTables.SQL_METADATA_VER2_CF_SQL);

        return hbaseOperations2.get(HBaseTables.SQL_METADATA_VER2, get, sqlMetaDataMapper);
    }

    private byte[] getDistributedKey(byte[] rowKey) {
        return rowKeyDistributorByHashPrefix.getDistributedKey(rowKey);
    }
    
    public void setSqlMetaDataMapper(RowMapper<List<SqlMetaDataBo>> sqlMetaDataMapper) {
        this.sqlMetaDataMapper = sqlMetaDataMapper;
    }
    
    public void setRowKeyDistributorByHashPrefix(RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        this.rowKeyDistributorByHashPrefix = rowKeyDistributorByHashPrefix;
    }
}
