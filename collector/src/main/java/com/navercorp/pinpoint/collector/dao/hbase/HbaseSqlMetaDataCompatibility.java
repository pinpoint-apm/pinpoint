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

package com.navercorp.pinpoint.collector.dao.hbase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.collector.dao.SqlMetaDataDao;
import com.navercorp.pinpoint.common.hbase.HBaseAdminTemplate;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;

/**
 * @author minwoo.jung
 */
//@Repository
public class HbaseSqlMetaDataCompatibility implements SqlMetaDataDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final boolean SQL_METADATA_VER2_EXISTED;

    @Autowired
    private SqlMetaDataDao hbaseSqlMetaDataPastVersionDao;
    
    @Autowired
    private SqlMetaDataDao hbaseSqlMetaDataDao;
    
    @Autowired
    public HbaseSqlMetaDataCompatibility(HBaseAdminTemplate hBaseAdminTemplate) {
        SQL_METADATA_VER2_EXISTED = hBaseAdminTemplate.tableExists(HBaseTables.SQL_METADATA_VER2);
        
        if (SQL_METADATA_VER2_EXISTED == false) {
            logger.warn("Please create 'SqlMetaData_Ver2' table.");
        }
        
        if(hBaseAdminTemplate.tableExists(HBaseTables.SQL_METADATA) == false && SQL_METADATA_VER2_EXISTED == false) {
            throw new RuntimeException("Please check for sqlMetaData_ver2 table in HBase. Need to create 'SqlMetaData_Ver2' table.");
        }
    }
    
    @Override
    public void insert(TSqlMetaData sqlMetaData) {
        if (SQL_METADATA_VER2_EXISTED) {
            hbaseSqlMetaDataDao.insert(sqlMetaData);
        } else {
            hbaseSqlMetaDataPastVersionDao.insert(sqlMetaData);
        }
    }
    
    public void setHbaseSqlMetaDataPastVersionDao(SqlMetaDataDao hbaseSqlMetaDataPastVersionDao) {
        this.hbaseSqlMetaDataPastVersionDao = hbaseSqlMetaDataPastVersionDao;
    }
    
    public void setHbaseSqlMetaDataDao(SqlMetaDataDao hbaseSqlMetaDataDao) {
        this.hbaseSqlMetaDataDao = hbaseSqlMetaDataDao;
    }
}
