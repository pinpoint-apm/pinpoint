/*
 * Copyright 2022 NAVER Corp.
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

package com.pinpoint.test.plugin.mongo;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

@Ignore
public class MongodbTest {
    private static MongoDBContainer container;

    @BeforeClass
    public static void beforeClass() {
//        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());
        container = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"));
        container.start();

        System.out.println("##host=" + container.getHost());
        System.out.println("##port=" + container.getFirstMappedPort());
    }

    @AfterClass
    public static void select() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    public void test() throws Exception {
        System.out.println("TEST");
        TimeUnit.HOURS.sleep(8);
    }
}
