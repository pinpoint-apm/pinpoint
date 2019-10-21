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

package com.navercorp.pinpoint.hbase.manager.hbase;

import com.navercorp.pinpoint.hbase.schema.dao.SchemaChangeLogDao;
import com.navercorp.pinpoint.hbase.schema.dao.hbase.HbaseSchemaChangeLogDao;
import com.navercorp.pinpoint.hbase.schema.domain.SchemaChangeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ReadOnlyHbaseSchemaChangeLogDao implements SchemaChangeLogDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseSchemaChangeLogDao hbaseSchemaChangeLogDao;

    public ReadOnlyHbaseSchemaChangeLogDao(HbaseSchemaChangeLogDao hbaseSchemaChangeLogDao) {
        this.hbaseSchemaChangeLogDao = Objects.requireNonNull(hbaseSchemaChangeLogDao, "hbaseSchemaChangeLogDao");
    }

    @Override
    public String getSchemaChangeLogTableName() {
        return hbaseSchemaChangeLogDao.getSchemaChangeLogTableName();
    }

    @Override
    public boolean tableExists(String namespace) {
        return hbaseSchemaChangeLogDao.tableExists(namespace);
    }

    @Override
    public boolean createTable(String namespace) {
        if (tableExists(namespace)) {
            logger.info("{} table already exists. Skip table creation.", getSchemaChangeLogTableName());
            return false;
        }
        logger.info("Creating {} table.", getSchemaChangeLogTableName());
        return true;
    }

    @Override
    public boolean resetTable(String namespace) {
        if (tableExists(namespace)) {
            logger.info("Resetting {} table.", getSchemaChangeLogTableName());
            return true;
        }
        logger.info("{} table does not exist. Cannot reset", getSchemaChangeLogTableName());
        return false;
    }

    @Override
    public void insertChangeLog(String namespace, SchemaChangeLog schemaChangeLog) {
        logger.info("Inserting schema change log, id : {}, order : {}, timestamp : {}",
                schemaChangeLog.getId(), schemaChangeLog.getExecOrder(), schemaChangeLog.getExecTimestamp());
    }

    @Override
    public List<SchemaChangeLog> getChangeLogs(String namespace) {
        return hbaseSchemaChangeLogDao.getChangeLogs(namespace);
    }

    @Override
    public SchemaChangeLog getChangeLog(String namespace, String id) {
        return hbaseSchemaChangeLogDao.getChangeLog(namespace, id);
    }
}
