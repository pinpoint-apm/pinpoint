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
import com.navercorp.pinpoint.common.hbase.HbaseSecurityProvider;
import com.navercorp.pinpoint.common.hbase.HbaseTableFactory;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate;
import com.navercorp.pinpoint.common.hbase.SimpleHbaseSecurityProvider;
import com.navercorp.pinpoint.common.hbase.TableFactory;
import com.navercorp.pinpoint.common.hbase.async.AsyncConnectionFactoryBean;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableCustomizer;
import com.navercorp.pinpoint.common.hbase.async.AsyncTableFactory;
import com.navercorp.pinpoint.common.hbase.async.DefaultAsyncTableCustomizer;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTableFactory;
import com.navercorp.pinpoint.common.hbase.async.HbaseAsyncTemplate;
import com.navercorp.pinpoint.common.hbase.scan.ResultScannerFactory;
import com.navercorp.pinpoint.common.hbase.util.EmptyScanMetricReporter;
import com.navercorp.pinpoint.common.hbase.util.ScanMetricReporter;
import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.CpuUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.security.User;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * @author HyunGil Jeong
 */
@org.springframework.context.annotation.Configuration
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

    @Value("${hbase.client.meta.operation.timeout:5000}")
    private String clientMetaOperationTimeout;

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

    public String getClientMetaOperationTimeout() {
        return clientMetaOperationTimeout;
    }

    public void setClientMetaOperationTimeout(String clientMetaOperationTimeout) {
        this.clientMetaOperationTimeout = clientMetaOperationTimeout;
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
    public FactoryBean<Configuration> hbaseConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("hbase.zookeeper.quorum", host);
        properties.setProperty("hbase.zookeeper.property.clientPort", port);
        properties.setProperty("zookeeper.znode.parent", znodeParent);
        properties.setProperty("zookeeper.recovery.retry", recoveryRetry);
        properties.setProperty("hbase.ipc.client.tcpnodelay", ipcClientTcpNoDelay);
        properties.setProperty("hbase.rpc.timeout", rpcTimeout);
        properties.setProperty("hbase.client.operation.timeout", clientOperationTimeout);
        properties.setProperty("hbase.client.meta.operation.timeout", clientMetaOperationTimeout);
        properties.setProperty("hbase.ipc.client.socket.timeout.read", ipcClientSocketTimeoutRead);
        properties.setProperty("hbase.ipc.client.socket.timeout.write", ipcClientSocketTimeoutWrite);
        HbaseConfigurationFactoryBean factoryBean = new HbaseConfigurationFactoryBean();
        factoryBean.setProperties(properties);
        return factoryBean;
    }


    @Bean
    @ConditionalOnProperty(name = "pinpoint.modules.hbase.security.auth", havingValue = "simple", matchIfMissing = true)
    public User hbaseLoginUser(Configuration configuration) {
        HbaseSecurityProvider securityProvider = new SimpleHbaseSecurityProvider(configuration);
        return securityProvider.login();
    }


    @Bean
    public FactoryBean<Connection> connectionFactory(Configuration configuration, User user) {
        return new ConnectionFactoryBean(configuration, user);
    }

    @Bean
    public TableFactory hbaseTableFactory(Connection connection) {
        return new HbaseTableFactory(connection);
    }

    @Bean
    public FactoryBean<AsyncConnection> hbaseAsyncConnectionFactory(Configuration configuration, User user) {
        return new AsyncConnectionFactoryBean(configuration, user);
    }

    @Bean
    public AsyncTableCustomizer asyncTableCustomizer() {
        return new DefaultAsyncTableCustomizer();
    }

    @Bean
    public AsyncTableFactory hbaseAsyncTableFactory(AsyncConnection connection, AsyncTableCustomizer customizer) {
        return new HbaseAsyncTableFactory(connection, customizer);
    }

    @Bean
    public HbaseAdminFactory hbaseAdminFactory(Connection connection) {
        return new HbaseAdminFactory(connection);
    }

    @Bean
    public HbaseAsyncTemplate hbaseAsyncTemplate(AsyncTableFactory asyncTableFactory) {
        ExecutorService executor = newAsyncTemplateExecutor();
        ScanMetricReporter scanMetricReporter = new EmptyScanMetricReporter();
        ResultScannerFactory scannerFactory = new ResultScannerFactory(4);
        return new HbaseAsyncTemplate(asyncTableFactory, scannerFactory, scanMetricReporter, executor);
    }

    private ExecutorService newAsyncTemplateExecutor() {
        ThreadFactory threadFactory = new PinpointThreadFactory("Pinpoint-asyncTemplate", true);
        return ExecutorFactory.newFixedThreadPool(CpuUtils.workerCount(), 1024*1024, threadFactory);
    }

    @Bean
    public HbaseTemplate hbaseTemplate(Configuration configuration,
                                       TableFactory hbaseTableFactory,
                                       HbaseAsyncTemplate asyncTemplate) {
        HbaseTemplate hbaseTemplate2 = new HbaseTemplate();
        hbaseTemplate2.setConfiguration(configuration);
        hbaseTemplate2.setTableFactory(hbaseTableFactory);
        hbaseTemplate2.setAsyncTemplate(asyncTemplate);
        return hbaseTemplate2;
    }
}
