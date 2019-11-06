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

package com.navercorp.pinpoint.common.hbase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.NamespaceExistException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class HBaseAdminTemplate implements HbaseAdminOperation {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AdminFactory adminFactory;

    public HBaseAdminTemplate(AdminFactory adminFactory) {
        this.adminFactory = Objects.requireNonNull(adminFactory, "adminFactory");
    }

    @Override
    public boolean createNamespaceIfNotExists(String namespace) {
        return execute(admin -> {
            NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(namespace).build();
            try {
                admin.createNamespace(namespaceDescriptor);
            } catch (NamespaceExistException e) {
                // ignored
                return false;
            }
            logger.info("{} namespace created.", namespace);
            return true;
        });
    }

    @Override
    public boolean createNamespaceIfNotExists(String namespace, Map<String, String> configurations) {
        return execute(admin -> {
            NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(namespace)
                    .addConfiguration(configurations).build();
            try {
                admin.createNamespace(namespaceDescriptor);
            } catch (NamespaceExistException e) {
                // ignored
                return false;
            }
            logger.info("{} namespace created.", namespace);
            return true;
        });
    }

    @Override
    public List<HTableDescriptor> getTableDescriptors(String namespace) {
        return execute(admin -> {
            HTableDescriptor[] htds = admin.listTableDescriptorsByNamespace(namespace);
            if (ArrayUtils.isEmpty(htds)) {
                return Collections.emptyList();
            }
            return Arrays.asList(htds);
        });
    }

    @Override
    public HTableDescriptor getTableDescriptor(TableName tableName) {
        return execute(admin -> admin.getTableDescriptor(tableName));
    }

    @Override
    public void createTable(HTableDescriptor htd) {
        execute(admin -> {
            admin.createTable(htd);
            logger.info("{} table created, htd : {}", htd.getTableName(), htd);
            return null;
        });
    }

    @Override
    public void createTable(HTableDescriptor htd, byte[][] splitKeys) {
        execute(admin -> {
            admin.createTable(htd, splitKeys);
            logger.info("{} table created with {} split keys, htd : {}", htd.getTableName(), splitKeys.length + 1, htd);
            return null;
        });
    }

    @Override
    public boolean createTableIfNotExists(HTableDescriptor htd) {
        return execute(admin -> {
            TableName tableName = htd.getTableName();
            if (!admin.tableExists(tableName)) {
                admin.createTable(htd);
                logger.info("{} table created, htd : {}", htd.getTableName(), htd);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean tableExists(TableName tableName) {
        return execute(admin -> admin.tableExists(tableName));
    }

    @Override
    public boolean truncateTable(TableName tableName, boolean preserveSplits) {
        return execute(admin -> {
            if (admin.tableExists(tableName)) {
                admin.disableTable(tableName);
                admin.truncateTable(tableName, preserveSplits);
                logger.info("{} table truncated.", tableName);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean dropTableIfExists(TableName tableName) {
        return execute(admin -> {
            if (admin.tableExists(tableName)) {
                admin.disableTable(tableName);
                admin.deleteTable(tableName);
                logger.info("{} table dropped.", tableName);
                return true;
            }
            return false;
        });
    }

    @Override
    public void dropTable(TableName tableName) {
        execute((AdminCallback<Void>) admin -> {
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            logger.info("{} table dropped.", tableName);
            return null;
        });
    }

    @Override
    public void modifyTable(HTableDescriptor htd) {
        final TableName tableName = htd.getTableName();
        execute(admin -> {
            admin.modifyTable(tableName, htd);
            logger.info("{} table modified, htd : {}", tableName, htd);
            return null;
        });
    }

    @Override
    public void addColumn(TableName tableName, HColumnDescriptor hcd) {
        execute(admin -> {
            admin.addColumn(tableName, hcd);
            logger.info("{} table added column : {}", tableName, hcd);
            return null;
        });
    }

    @Override
    public final <T> T execute(AdminCallback<T> action) {
        Objects.requireNonNull(action, "action");
        Admin admin = adminFactory.getAdmin();
        try {
            return action.doInAdmin(admin);
        } catch (Throwable e) {
            if (e instanceof Error) {
                throw (Error) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new HbaseSystemException((Exception) e);
        } finally {
            adminFactory.releaseAdmin(admin);
        }
    }
}
