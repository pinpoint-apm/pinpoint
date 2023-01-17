/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.lettuce;

import com.navercorp.pinpoint.pluginit.utils.SocketUtils;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class RedisServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    private redis.embedded.RedisServer redisServer;

    @Override
    public Properties beforeAll() {
        final int port = SocketUtils.findAvailableTcpPort(1000, 2000);
        redisServer = new redis.embedded.RedisServer(port);

        redisServer.start();

        Properties properties = new Properties();
        properties.setProperty("PORT", String.valueOf(port));
        return properties;
    }

    @Override
    public void afterAll() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
