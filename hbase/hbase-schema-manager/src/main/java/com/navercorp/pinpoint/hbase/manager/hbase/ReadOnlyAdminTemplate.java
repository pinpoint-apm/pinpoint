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

import com.navercorp.pinpoint.common.hbase.AdminCallback;
import com.navercorp.pinpoint.common.hbase.HbaseAdminOperation;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ReadOnlyAdminTemplate implements HbaseAdminOperation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HbaseAdminOperation delegate;

    public ReadOnlyAdminTemplate(HbaseAdminOperation delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public boolean createNamespaceIfNotExists(String namespace) {
        boolean namespaceCreated = delegate.createNamespaceIfNotExists(namespace);
        if (namespaceCreated) {
            logger.info("Creating namespace : {}", namespace);
        }
        return namespaceCreated;
    }

    @Override
    public boolean createNamespaceIfNotExists(String namespace, Map<String, String> configurations) {
        boolean namespaceCreated = delegate.createNamespaceIfNotExists(namespace, configurations);
        if (namespaceCreated) {
            logger.info("Creating namespace : {}, configurations : {}.", namespace, configurations);
        }
        return namespaceCreated;
    }

    @Override
    public List<HTableDescriptor> getTableDescriptors(String namespace) {
        return delegate.getTableDescriptors(namespace);
    }

    @Override
    public HTableDescriptor getTableDescriptor(TableName tableName) {
        return delegate.getTableDescriptor(tableName);
    }

    @Override
    public void createTable(HTableDescriptor htd) {
        logger.info("Creating table : {}.", htd);
    }

    @Override
    public void createTable(HTableDescriptor htd, byte[][] splitKeys) {
        logger.info("Creating table : {} with {} splitKeys.", htd, splitKeys.length);
    }

    @Override
    public boolean createTableIfNotExists(HTableDescriptor htd) {
        TableName tableName = htd.getTableName();
        boolean tableExists = delegate.tableExists(tableName);
        if (tableExists) {
            return false;
        }
        this.createTable(htd);
        return true;
    }

    @Override
    public boolean tableExists(TableName tableName) {
        return delegate.tableExists(tableName);
    }

    @Override
    public boolean truncateTable(TableName tableName, boolean preserveSplits) {
        boolean tableExists = delegate.tableExists(tableName);
        if (tableExists) {
            if (preserveSplits) {
                logger.info("Truncating table : {}, preserving splits", tableName);
            } else {
                logger.info("Truncating table : {}, not preserving splits", tableName);
            }
            return true;
        }
        logger.info("Tried to truncate {}, but table does not exist", tableName);
        return false;
    }

    @Override
    public boolean dropTableIfExists(TableName tableName) {
        boolean tableExists = delegate.tableExists(tableName);
        if (tableExists) {
            logger.info("Dropping table : {}", tableName);
            return true;
        }
        logger.info("Tried to drop : {}, but table does not exist", tableName);
        return false;
    }

    @Override
    public void dropTable(TableName tableName) {
        logger.info("Dropping table : {}", tableName);
    }

    @Override
    public void modifyTable(HTableDescriptor htd) {
        logger.info("Modifying table : {}, desc : {}", htd.getTableName(), htd);
    }

    @Override
    public void addColumn(TableName tableName, HColumnDescriptor hcd) {
        logger.info("Adding column to table : {}, column : {}", tableName, hcd);
    }

    @Override
    public <T> T execute(AdminCallback<T> action) {
        throw new UnsupportedOperationException();
    }
}
