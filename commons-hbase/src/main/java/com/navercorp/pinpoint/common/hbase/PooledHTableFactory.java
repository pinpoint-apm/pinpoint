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

import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.shaded.org.apache.directory.shared.kerberos.exceptions.ErrorType;
import org.apache.hadoop.hbase.shaded.org.apache.directory.shared.kerberos.exceptions.KerberosException;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * HTableInterfaceFactory based on HTablePool.
 * @author emeroad
 * @autor minwoo.jung
 */
public class PooledHTableFactory implements TableFactory, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExecutorService executor;
    private final Connection connection;

    public PooledHTableFactory(Configuration config, ExecutorService executor) {
        Objects.requireNonNull(config, "config must not be null");
        this.executor = Objects.requireNonNull(executor, "executor must not be null");

        try {
            String authEnable = config.get("hbase.kerberos.auth");
            if (!StringUtils.isEmpty(authEnable) && Boolean.parseBoolean(authEnable)){
                if (StringUtils.isEmpty(config.get("hbase.kerberos.keytab.path"))){
                    throw new KerberosException(ErrorType.KDC_ERR_NONE);
                }
                System.out.println("hbase.kerberos.keytab.path:"+config.get("hbase.zookeeper.quorum"));
                System.out.println("hbase.kerberos.keytab.path:"+config.get("hbase.kerberos.keytab.path"));
                System.out.println("hbase.kerberos.user:"+config.get("hbase.kerberos.user"));

                config.set("hbase.security.authentication", "kerberos");
                config.set("hadoop.security.authentication", "kerberos");
                config.set("hbase.master.kerberos.principal",config.get("hbase.master.kerberos.principal"));
                config.set("hbase.regionserver.kerberos.principal",config.get("hbase.regionserver.kerberos.principal"));
                UserGroupInformation.setConfiguration(config);
                String kerberosUser = config.get("hbase.kerberos.user");
                String kerberosPath = config.get("hbase.kerberos.keytab.path");
                UserGroupInformation.loginUserFromKeytab(kerberosUser, kerberosPath);
            }
            this.connection = ConnectionFactory.createConnection(config, executor);
        } catch (Exception e) {
            throw new HbaseSystemException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }


    @Override
    public Table getTable(TableName tableName) {
        try {
            return connection.getTable(tableName, executor);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    @Override
    public Table getTable(TableName tableName, ExecutorService executorService) {
        try {
            return connection.getTable(tableName, executorService);
        } catch (IOException e) {
            throw new HbaseSystemException(e);
        }
    }

    @Override
    public void releaseTable(Table table) {
        if (table == null) {
            return;
        }

        try {
            table.close();
        } catch (IOException ex) {
            throw new HbaseSystemException(ex);
        }
    }


    @Override
    public void destroy() throws Exception {
        logger.info("PooledHTableFactory.destroy()");
        
        if (connection != null) {
            try {
                this.connection.close();
            } catch (IOException ex) {
                logger.warn("Connection.close() error:" + ex.getMessage(), ex);
            }
        }
    }
}
