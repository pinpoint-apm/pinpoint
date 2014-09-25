package com.nhn.pinpoint.common.hbase;

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
