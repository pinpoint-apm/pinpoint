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
import com.navercorp.pinpoint.common.util.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

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
        Assertions.assertEquals(HBASE_CLIENT_HOST_VALUE, clientHost);

        String clientPort = properties.getProperty("hbase.client.port");
        Assertions.assertEquals("2181", clientPort);
    }

    @Test
    public void checkCollectionPropertiesTest() throws Exception {
        Properties properties = PropertyUtils.loadPropertyFromClassPath("pinpoint-collector-root.properties");

        String receiverIp = properties.getProperty("collector.receiver.base.ip");
        Assertions.assertEquals("0.0.0.0", receiverIp);

        receiverIp = properties.getProperty("collector.receiver.stat.udp.ip");
        Assertions.assertEquals("0.0.0.0", receiverIp);

        receiverIp = properties.getProperty("collector.receiver.stat.tcp.ip");
        Assertions.assertEquals("0.0.0.0", receiverIp);

        receiverIp = properties.getProperty("collector.receiver.span.udp.ip");
        Assertions.assertEquals("0.0.0.0", receiverIp);

        receiverIp = properties.getProperty("collector.receiver.span.tcp.ip");
        Assertions.assertEquals("0.0.0.0", receiverIp);

        String l4Ip = properties.getProperty("collector.l4.ip");
        Assertions.assertTrue(StringUtils.isEmpty(l4Ip));

        String pinpointZKAddress = properties.getProperty("pinpoint.zookeeper.address");
        Assertions.assertEquals(LOCAL_HOST, pinpointZKAddress);

        String zookeeperAddress = properties.getProperty("cluster.zookeeper.address");
        Assertions.assertEquals(HBASE_CLIENT_HOST_VALUE, zookeeperAddress);

        zookeeperAddress = properties.getProperty("flink.cluster.zookeeper.address");
        Assertions.assertEquals(HBASE_CLIENT_HOST_VALUE, zookeeperAddress);

        String clusterListenIp = properties.getProperty("cluster.listen.ip");
        Assertions.assertTrue(StringUtils.isEmpty(clusterListenIp));
    }

}
