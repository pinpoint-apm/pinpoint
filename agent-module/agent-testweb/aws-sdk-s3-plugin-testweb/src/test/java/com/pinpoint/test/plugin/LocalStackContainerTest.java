/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@Disabled
public class LocalStackContainerTest {
    private static LocalStackContainer container;

    @BeforeAll
    public static void setUp() {
        DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:3.5.0");
        container = new LocalStackContainer(localstackImage)
                .withServices(LocalStackContainer.Service.S3);
        container.start();

        System.out.println("AccessKey=" + container.getAccessKey());
        System.out.println("SecretKey=" + container.getSecretKey());
        System.out.println("Region=" + container.getRegion());
        System.out.println("EndPoint=" + container.getEndpoint());
    }

    @AfterAll
    public static void cleanUp() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    public void test() throws InterruptedException {
        // This is just a placeholder test to ensure the container is running
        System.out.println("LocalStack container is running");
    }
}
