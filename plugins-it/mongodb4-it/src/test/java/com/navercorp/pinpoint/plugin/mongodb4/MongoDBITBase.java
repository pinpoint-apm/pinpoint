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

package com.navercorp.pinpoint.plugin.mongodb4;

import org.testcontainers.containers.MongoDBContainer;

/**
 * @author Roy Kim
 */
public abstract class MongoDBITBase {
    protected static MongoDBContainer container;

    protected static String HOST;
    protected static int PORT;

    abstract Class<?> getMongoDatabaseClazz() throws ClassNotFoundException;

    public static void sharedTeardown() throws Exception {
        if (container != null) {
            container.stop();
        }
        awaitCompleted();
    }

    public static void setHost(String host) {
        HOST = host;
    }

    public static String getHost() {
        return HOST;
    }

    public static void setPort(int port) {
        PORT = port;
    }

    public static int getPort() {
        return PORT;
    }


    private static void awaitCompleted() throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            if (!container.isRunning()) {
                break;
            }
            Thread.sleep(10);
        }
    }
}
