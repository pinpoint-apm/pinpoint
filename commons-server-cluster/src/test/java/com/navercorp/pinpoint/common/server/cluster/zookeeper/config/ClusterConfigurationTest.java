/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.cluster.zookeeper.config;

import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClusterConfiguration;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperConstants;
import org.apache.curator.utils.ZKPaths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@EnableConfigurationProperties
@TestPropertySource(locations = "classpath:test-cluster.properties")
@ContextConfiguration(classes = ClusterConfigurationFactory.class)
@ExtendWith(SpringExtension.class)
public class ClusterConfigurationTest {

    @Autowired
    @Qualifier("clusterConfiguration")
    ZookeeperClusterConfiguration defaultClusterConfiguration;

    @Autowired
    @Qualifier("flinkClusterConfiguration")
    ZookeeperClusterConfiguration flinkClusterConfiguration;

    @Autowired
    @Qualifier("flinkSpanStatClusterConfiguration")
    ZookeeperClusterConfiguration spanStatFlinkClusterConfiguration;

    @Test
    public void clusterConfigurationTest() {
        ZNodePathFactory zNodePathFactory = new ZNodePathFactory("/cluster");

        Assertions.assertTrue(defaultClusterConfiguration.isEnable());
        Assertions.assertEquals("localhost", defaultClusterConfiguration.getAddress());
        Assertions.assertEquals(zNodePathFactory.create("webs"), defaultClusterConfiguration.getWebZNodePath());
        Assertions.assertEquals(zNodePathFactory.createCollectorPath(), defaultClusterConfiguration.getCollectorZNodePath());
        Assertions.assertEquals(zNodePathFactory.createFlinkPath(), defaultClusterConfiguration.getFlinkZNodePath());
        Assertions.assertEquals(1000, defaultClusterConfiguration.getSessionTimeout());
    }

    @Test
    public void flinkClusterConfigurationTest() {
        ZNodePathFactory zNodePathFactory = new ZNodePathFactory("/flink-cluster");

        Assertions.assertFalse(flinkClusterConfiguration.isEnable());
        Assertions.assertEquals("127.0.0.1", flinkClusterConfiguration.getAddress());
        Assertions.assertEquals(zNodePathFactory.createWebPath(), flinkClusterConfiguration.getWebZNodePath());
        Assertions.assertEquals(zNodePathFactory.create("collectors"), flinkClusterConfiguration.getCollectorZNodePath());
        Assertions.assertEquals(zNodePathFactory.createFlinkPath(), flinkClusterConfiguration.getFlinkZNodePath());
        Assertions.assertEquals(2000, flinkClusterConfiguration.getSessionTimeout());
    }

    @Test
    public void spanStatFlinkClusterConfigurationTest() {
        ZNodePathFactory zNodePathFactory = new ZNodePathFactory("/span-stat-flink-cluster");

        Assertions.assertTrue(defaultClusterConfiguration.isEnable());
        Assertions.assertEquals("0.0.0.0", spanStatFlinkClusterConfiguration.getAddress());
        Assertions.assertEquals(zNodePathFactory.createWebPath(), spanStatFlinkClusterConfiguration.getWebZNodePath());
        Assertions.assertEquals(zNodePathFactory.createCollectorPath(), spanStatFlinkClusterConfiguration.getCollectorZNodePath());
        Assertions.assertEquals(zNodePathFactory.create("flinks"), spanStatFlinkClusterConfiguration.getFlinkZNodePath());
        Assertions.assertEquals(3000, spanStatFlinkClusterConfiguration.getSessionTimeout());
    }

    private static class ZNodePathFactory {

        private final String zNodeRoot;

        private ZNodePathFactory(String zNodeRoot) {
            this.zNodeRoot = Objects.requireNonNull(zNodeRoot, "zNodeRoot");
        }

        private String create(String child) {
            return ZKPaths.makePath(zNodeRoot, child);
        }

        private String createWebPath() {
            return ZKPaths.makePath(zNodeRoot, ZookeeperConstants.WEB_LEAF_PATH);
        }

        private String createCollectorPath() {
            return ZKPaths.makePath(zNodeRoot, ZookeeperConstants.COLLECTOR_LEAF_PATH);
        }

        private String createFlinkPath() {
            return ZKPaths.makePath(zNodeRoot, ZookeeperConstants.FLINK_LEAF_PATH);
        }

    }


}
