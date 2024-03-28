/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.utils;

import com.github.dockerjava.api.model.Info;
import org.testcontainers.DockerClientFactory;

/**
 * @author youngjin.kim2
 */
public class DockerTestUtils {

    public static boolean isArmDockerServer() {
        DockerClientFactory dockerClientFactory = DockerClientFactory.instance();
        Info dockerInfo = dockerClientFactory.getInfo();
        String arch = dockerInfo.getArchitecture();
        if (arch == null) {
            throw new RuntimeException("Failed to get docker architecture");
        }
        return arch.contains("aarch") || arch.contains("arm");
    }

}
