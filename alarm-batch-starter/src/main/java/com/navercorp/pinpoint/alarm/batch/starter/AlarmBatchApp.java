/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.batch.starter;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Evaluates alarm rules and sends what they produce, as a process of its own.
 *
 * <p>Two schedules run here and they do not wait on each other: evaluation records an event
 * and enqueues its deliveries in one transaction, and a separate schedule drains that queue.
 * A slow notification provider therefore cannot delay the next evaluation, and a failed send
 * cannot roll back the event that caused it.
 *
 * <p>What a rule may measure is not decided by the job; see
 * {@link com.navercorp.pinpoint.alarm.batch.config.AlarmJobConfiguration} for how a rule is
 * routed and what the sweep does and does not read. The core data sources and the stores
 * they read are the other half of this process; the job half boots without them.
 */
@SpringBootConfiguration
// Paired with @SpringBootConfiguration above, which is what marks a class as the entry point
// to a reader and to the tooling that offers to run it. Declared only here: the imported
// configurations carry none, so importing one of them elsewhere does not switch these off in
// whatever context did the importing.
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        BatchAutoConfiguration.class
})
@Import({
        AlarmBatchJobConfiguration.class,

        AlarmBatchHbaseConfiguration.class,
        AlarmBatchPinotConfiguration.class,
        AlarmBatchCoreDataSourceConfiguration.class
})
public class AlarmBatchApp {

    private static final ServerBootLogger logger = ServerBootLogger.getLogger(AlarmBatchApp.class);

    public static void main(String[] args) {
        try {
            AlarmBatchStarter starter = new AlarmBatchStarter(AlarmBatchApp.class);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[AlarmBatchApp] could not launch app.", exception);
        }
    }
}
