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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
@EnableConfigurationProperties
@TestPropertySource(locations = "classpath:test-cluster.properties")
@ContextConfiguration(classes = ClusterConfigurationFactory.class)
@RunWith(SpringJUnit4ClassRunner.class)
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

        Assert.assertTrue(defaultClusterConfiguration.isEnable());
        Assert.assertEquals("localhost", defaultClusterConfiguration.getAddress());
        Assert.assertEquals(zNodePathFactory.create("webs"), defaultClusterConfiguration.getWebZNodePath());
        Assert.assertEquals(zNodePathFactory.createCollectorPath(), defaultClusterConfiguration.getCollectorZNodePath());
        Assert.assertEquals(zNodePathFactory.createFlinkPath(), defaultClusterConfiguration.getFlinkZNodePath());
        Assert.assertEquals(1000, defaultClusterConfiguration.getSessionTimeout());
    }

    @Test
    public void flinkClusterConfigurationTest() {
        ZNodePathFactory zNodePathFactory = new ZNodePathFactory("/flink-cluster");

        Assert.assertFalse(flinkClusterConfiguration.isEnable());
        Assert.assertEquals("127.0.0.1", flinkClusterConfiguration.getAddress());
        Assert.assertEquals(zNodePathFactory.createWebPath(), flinkClusterConfiguration.getWebZNodePath());
        Assert.assertEquals(zNodePathFactory.create("collectors"), flinkClusterConfiguration.getCollectorZNodePath());
        Assert.assertEquals(zNodePathFactory.createFlinkPath(), flinkClusterConfiguration.getFlinkZNodePath());
        Assert.assertEquals(2000, flinkClusterConfiguration.getSessionTimeout());
    }

    @Test
    public void spanStatFlinkClusterConfigurationTest() {
        ZNodePathFactory zNodePathFactory = new ZNodePathFactory("/span-stat-flink-cluster");

        Assert.assertTrue(defaultClusterConfiguration.isEnable());
        Assert.assertEquals("0.0.0.0", spanStatFlinkClusterConfiguration.getAddress());
        Assert.assertEquals(zNodePathFactory.createWebPath(), spanStatFlinkClusterConfiguration.getWebZNodePath());
        Assert.assertEquals(zNodePathFactory.createCollectorPath(), spanStatFlinkClusterConfiguration.getCollectorZNodePath());
        Assert.assertEquals(zNodePathFactory.create("flinks"), spanStatFlinkClusterConfiguration.getFlinkZNodePath());
        Assert.assertEquals(3000, spanStatFlinkClusterConfiguration.getSessionTimeout());
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
