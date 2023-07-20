/*
 * Copyright 2023 NAVER Corp.
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
package com.pinpoint.test.plugin.spring.data.r2dbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author youngjin.kim2
 */
public class DockerTestUtils {
    public static boolean isArmDockerServer() {
        return getDockerArchitecture().contains("arm");
    }

    private static String getDockerArchitecture() {
        final String dockerInfo = execute("docker version", 3000);
        int serverIndex = dockerInfo.indexOf("Server: ");
        if (serverIndex != -1) {
            final String dockerServerInfo = dockerInfo.substring(serverIndex);
            final String archLine = Arrays.stream(dockerServerInfo.split("\n"))
                    .filter(line -> line.contains("OS/Arch:"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Invalid docker version result"));
            return archLine.split(":")[1].trim();
        }
        return dockerInfo;
    }

    private static String execute(String command, long waitMillis) {
        try {
            return execute0(command, waitMillis);
        } catch (Throwable th) {
            throw new RuntimeException("Failed to run '" + command + "'");
        }
    }

    private static String execute0(String command, long waitMillis) throws InterruptedException, IOException {
        final Process proc = Runtime.getRuntime().exec(command);
        if (!proc.waitFor(waitMillis, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException();
        }
        return readAll(proc.getInputStream());
    }

    private static String readAll(InputStream is) {
        return new BufferedReader(new InputStreamReader(is))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
