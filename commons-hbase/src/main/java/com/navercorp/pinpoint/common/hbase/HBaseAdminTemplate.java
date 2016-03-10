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

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.data.hadoop.hbase.HbaseSystemException;

/**
 * @author emeroad
 */
public class HBaseAdminTemplate {

    private final Admin admin;
    private final Connection connection;

    public HBaseAdminTemplate(Configuration configuration) {
        try {
            connection = ConnectionFactory.createConnection(configuration);
            admin = connection.getAdmin();
        } catch (Exception e) {
            throw new HbaseSystemException(e);
        }
    }

    public boolean createTableIfNotExist(HTableDescriptor htd) {
        try {
            if (!admin.tableExists(htd.getTableName())) {
                this.admin.createTable(htd);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    public boolean tableExists(String tableName) {
        try {
            return admin.tableExists(TableName.valueOf(tableName));
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    public boolean dropTableIfExist(String tableName) {
        TableName tn = TableName.valueOf(tableName);
        try {
            if (admin.tableExists(tn)) {
                this.admin.disableTable(tn);
                this.admin.deleteTable(tn);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    public void dropTable(String tableName) {
        TableName tn = TableName.valueOf(tableName);
        try {
            this.admin.disableTable(tn);
            this.admin.deleteTable(tn);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    public void close() {
        try {
            this.admin.close();
            this.connection.close();
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }
}
