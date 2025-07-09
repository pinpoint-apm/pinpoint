/*
 * Copyright 2022 NAVER Corp.
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

package com.pinpoint.test.plugin.cassandra;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.cassandra.CassandraContainer;

import java.net.InetSocketAddress;

@Disabled
public class CassandraTest {

    @AutoClose
    private static CassandraContainer container;

    @BeforeAll
    public static void beforeClass() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        container = new CassandraContainer("cassandra:3.11.6");
        container.start();

        container.getLocalDatacenter();
        InetSocketAddress contactPoint = container.getContactPoint();
        System.out.println("##contactPoint=" + contactPoint);
        System.out.println("##LocalDatacenter=" + container.getLocalDatacenter());
        System.out.println("##user=" + container.getUsername());
        System.out.println("##password=" + container.getPassword());
    }


    @Test
    public void test() {
        System.out.println("TEST " + container.getContactPoint());
    }
}