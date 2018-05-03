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
import java.util.Objects;

import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class HBaseAdminTemplate {

    private final AdminFactory adminFactory;

    public HBaseAdminTemplate(AdminFactory adminFactory) {
        this.adminFactory = Objects.requireNonNull(adminFactory, "adminFactory must not be null");
    }

    public boolean createTableIfNotExist(HTableDescriptor htd) {
        Admin admin = adminFactory.getAdmin();
        try {
            TableName tableName = htd.getTableName();
            if (!admin.tableExists(tableName)) {
                admin.createTable(htd);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        } finally {
            adminFactory.releaseAdmin(admin);
        }
    }

    public boolean tableExists(TableName tableName) {
        Admin admin = adminFactory.getAdmin();
        try {
            return admin.tableExists(tableName);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        } finally {
            adminFactory.releaseAdmin(admin);
        }
    }

    public boolean dropTableIfExist(TableName tableName) {
        Admin admin = adminFactory.getAdmin();
        try {
            if (admin.tableExists(tableName)) {
                admin.disableTable(tableName);
                admin.deleteTable(tableName);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        } finally {
            adminFactory.releaseAdmin(admin);
        }
    }

    public void dropTable(TableName tableName) {
        Admin admin = adminFactory.getAdmin();
        try {
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        } finally {
            adminFactory.releaseAdmin(admin);
        }
    }
}
