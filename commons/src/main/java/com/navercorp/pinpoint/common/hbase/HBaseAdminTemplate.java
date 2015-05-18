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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.springframework.data.hadoop.hbase.HbaseSystemException;

import java.io.IOException;

/**
 * @author emeroad
 */
public class HBaseAdminTemplate {

    private final HBaseAdmin hBaseAdmin;

    public HBaseAdminTemplate(Configuration configuration) {
        try {
            this.hBaseAdmin = new HBaseAdmin(configuration);
        } catch (MasterNotRunningException e) {
            throw new HbaseSystemException(e);
        } catch (ZooKeeperConnectionException e) {
            throw new HbaseSystemException(e);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    public boolean createTableIfNotExist(HTableDescriptor htd) {
        try {
            if (!hBaseAdmin.tableExists(htd.getName())) {
                this.hBaseAdmin.createTable(htd);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    public boolean tableExists(String tableName) {
        try {
            return hBaseAdmin.tableExists(tableName);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    public boolean dropTableIfExist(String tableName) {
        try {
            if (hBaseAdmin.tableExists(tableName)) {
                this.hBaseAdmin.disableTable(tableName);
                this.hBaseAdmin.deleteTable(tableName);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    public void dropTable(String tableName) {
        try {
            this.hBaseAdmin.disableTable(tableName);
            this.hBaseAdmin.deleteTable(tableName);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    public void close() {
        try {
            this.hBaseAdmin.close();
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }
}
