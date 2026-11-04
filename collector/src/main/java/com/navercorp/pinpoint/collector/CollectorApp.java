/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.datasource.MainDataSourceConfiguration;
import com.navercorp.pinpoint.datasource.MainDataSourcePropertySource;
import com.navercorp.pinpoint.redis.RedisPropertySources;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({
        PinpointCollectorModule.class,

        MainDataSourcePropertySource.class,
        MainDataSourceConfiguration.class,

        RedisPropertySources.class,
})
public class CollectorApp {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(CollectorApp.class);

    public static void main(String[] args) {
        try {
            CollectorStarter starter = new CollectorStarter(CollectorApp.class);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[CollectorApp] could not launch app.", exception);
        }
    }

}
