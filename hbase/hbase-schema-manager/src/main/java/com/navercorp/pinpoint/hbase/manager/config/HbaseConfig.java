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

package com.navercorp.pinpoint.hbase.manager.config;

import com.navercorp.pinpoint.common.hbase.ConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.HbaseAdminFactory;
import com.navercorp.pinpoint.common.hbase.HbaseConfigurationFactoryBean;
import com.navercorp.pinpoint.common.hbase.HbaseTableFactory;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import org.apache.hadoop.hbase.client.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;

/**
 * @author HyunGil Jeong
 */
@Configuration
@PropertySource("classpath:hbase.properties")
public class HbaseConfig {

    @Value("${hbase.host:localhost}")
    private String host;

    @Value("${hbase.port:2181}")
    private String port;

    @Value("${hbase.znodeParent:/hbase}")
    private String znodeParent;

    @Value("${hbase.recoveryRetry:3}")
    private String recoveryRetry;

    @Value("${hbase.ipcClientTpcNoDelay:true}")
    private String ipcClientTcpNoDelay;

    @Value("${hbase.rpc.timeout:3000}")
    private String rpcTimeout;

    @Value("${hbase.client.operation.timeout:5000}")
    private String clientOperationTimeout;

    @Value("${hbase.ipc.client.socket.timeout.read:5000}")
    private String ipcClientSocketTimeoutRead;

    @Value("${hbase.ipc.client.socket.timeout.write:5000}")
    private String ipcClientSocketTimeoutWrite;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getZnodeParent() {
        return znodeParent;
    }

    public void setZnodeParent(String znodeParent) {
        this.znodeParent = znodeParent;
    }

    public String getRecoveryRetry() {
        return recoveryRetry;
    }

    public void setRecoveryRetry(String recoveryRetry) {
        this.recoveryRetry = recoveryRetry;
    }

    public String getIpcClientTcpNoDelay() {
        return ipcClientTcpNoDelay;
    }

    public void setIpcClientTcpNoDelay(String ipcClientTcpNoDelay) {
        this.ipcClientTcpNoDelay = ipcClientTcpNoDelay;
    }

    public String getRpcTimeout() {
        return rpcTimeout;
    }

    public void setRpcTimeout(String rpcTimeout) {
        this.rpcTimeout = rpcTimeout;
    }

    public String getClientOperationTimeout() {
        return clientOperationTimeout;
    }

    public void setClientOperationTimeout(String clientOperationTimeout) {
        this.clientOperationTimeout = clientOperationTimeout;
    }

    public String getIpcClientSocketTimeoutRead() {
        return ipcClientSocketTimeoutRead;
    }

    public void setIpcClientSocketTimeoutRead(String ipcClientSocketTimeoutRead) {
        this.ipcClientSocketTimeoutRead = ipcClientSocketTimeoutRead;
    }

    public String getIpcClientSocketTimeoutWrite() {
        return ipcClientSocketTimeoutWrite;
    }

    public void setIpcClientSocketTimeoutWrite(String ipcClientSocketTimeoutWrite) {
        this.ipcClientSocketTimeoutWrite = ipcClientSocketTimeoutWrite;
    }

    @Bean
    public HbaseConfigurationFactoryBean hbaseConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("hbase.zookeeper.quorum", host);
        properties.setProperty("hbase.zookeeper.property.clientPort", port);
        properties.setProperty("zookeeper.znode.parent", znodeParent);
        properties.setProperty("zookeeper.recovery.retry", recoveryRetry);
        properties.setProperty("hbase.ipc.client.tcpnodelay", ipcClientTcpNoDelay);
        properties.setProperty("hbase.rpc.timeout", rpcTimeout);
        properties.setProperty("hbase.client.operation.timeout", clientOperationTimeout);
        properties.setProperty("hbase.ipc.client.socket.timeout.read", ipcClientSocketTimeoutRead);
        properties.setProperty("hbase.ipc.client.socket.timeout.write", ipcClientSocketTimeoutWrite);
        HbaseConfigurationFactoryBean factoryBean = new HbaseConfigurationFactoryBean();
        factoryBean.setProperties(properties);
        return factoryBean;
    }

    @Bean
    public ConnectionFactoryBean connectionFactory(org.apache.hadoop.conf.Configuration configuration) {
        return new ConnectionFactoryBean(configuration);
    }

    @Bean
    public HbaseTableFactory hbaseTableFactory(Connection connection) {
        return new HbaseTableFactory(connection);
    }

    @Bean
    public HbaseAdminFactory hbaseAdminFactory(Connection connection) {
        return new HbaseAdminFactory(connection);
    }

    @Bean
    public HbaseTemplate2 hbaseTemplate(org.apache.hadoop.conf.Configuration configuration,
                                        HbaseTableFactory hbaseTableFactory) {
        HbaseTemplate2 hbaseTemplate2 = new HbaseTemplate2();
        hbaseTemplate2.setConfiguration(configuration);
        hbaseTemplate2.setTableFactory(hbaseTableFactory);
        return hbaseTemplate2;
    }
}
