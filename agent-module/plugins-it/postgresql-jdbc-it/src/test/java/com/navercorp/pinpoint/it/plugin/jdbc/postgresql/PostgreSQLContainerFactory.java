/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.it.plugin.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.OutputFrame;

import java.util.function.Consumer;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PostgreSQLContainerFactory {

    public static PostgreSQLContainer newContainer(String loggerName) {
        PostgreSQLContainer container = new PostgreSQLContainer();
        container.withInitScript("init_postgresql.sql");

        container.withLogConsumer(new Consumer<OutputFrame>() {
            private final Logger logger = LogManager.getLogger();
            @Override
            public void accept(OutputFrame outputFrame) {
                logger.info(LogUtils.removeLineBreak(outputFrame.getUtf8String()));
            }
        });
        return container;
    }
}
