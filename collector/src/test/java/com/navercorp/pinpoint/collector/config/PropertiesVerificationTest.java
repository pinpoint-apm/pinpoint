/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.util.PropertyUtils;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Taejin Koo
 */
public class PropertiesVerificationTest {

    private static final String LOCAL_HOST = "localhost";
    private static final String HBASE_CLIENT_HOST_VALUE = "${pinpoint.zookeeper.address}";

    @Test
    public void checkHbasePropertiesTest() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("profiles/release/hbase.properties");

        String clientHost = properties.getProperty("hbase.client.host");
        assertEquals(HBASE_CLIENT_HOST_VALUE, clientHost);

        String clientPort = properties.getProperty("hbase.client.port");
        assertEquals("2181", clientPort);
    }

    @Test
    public void checkCollectionPropertiesTest() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("pinpoint-collector-root.properties");

        String receiverIp = properties.getProperty("pinpoint.zookeeper.address");
        assertEquals("localhost", receiverIp);

    }

}
