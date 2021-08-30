/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.collector.receiver.BindAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@EnableConfigurationProperties
@TestPropertySource(locations = "classpath:test-pinpoint-collector.properties")
@ContextConfiguration(classes = {GrpcAgentDataSslReceiverConfigurationFactory.class,GrpcStatSslReceiverConfigurationFactory.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class GrpcStatSslReceiverConfigurationTest {

    @Autowired
    @Qualifier(GrpcStatSslReceiverConfigurationFactory.STAT_SSL_CONFIG)
    private GrpcSslReceiverConfiguration configuration;

    @Test
    public void properties() {
        assertEquals(Boolean.FALSE, configuration.isEnable());
        BindAddress bindAddress = configuration.getBindAddress();
        assertEquals("2.2.2.2", bindAddress.getIp());
        assertEquals(29442, bindAddress.getPort());
    }

    @Test
    public void grpcSslConfiguration() throws URISyntaxException {
        GrpcSslConfiguration sslConfiguration = configuration.getGrpcSslConfiguration();

        assertEquals(Boolean.TRUE, sslConfiguration.isEnable());
        assertEquals("jdk", sslConfiguration.getProviderType());

        URL keyFileUrl = sslConfiguration.getKeyFileUrl();
        File keyFile = new File(keyFileUrl.toURI());
        assertEquals("server0.pem", keyFile.getName());
        assertEquals(Boolean.TRUE, keyFile.exists());

        URL keyCertFileUrl = sslConfiguration.getKeyCertFileUrl();
        File keyCertFile = new File(keyCertFileUrl.toURI());
        assertEquals("server0.key", keyCertFile.getName());
        assertEquals(Boolean.TRUE, keyCertFile.exists());
    }
}