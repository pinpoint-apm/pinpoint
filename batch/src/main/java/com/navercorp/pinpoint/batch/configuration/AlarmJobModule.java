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
 *
 */

package com.navercorp.pinpoint.batch.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;


@ImportResource({
        "classpath:job/applicationContext-alarmJob.xml",
})
@ComponentScan({
        "com.navercorp.pinpoint.batch.alarm"
})
@Import({
        AlarmCheckerConfiguration.class,
        BatchPinotDaoConfiguration.class
})
@Configuration
public class AlarmJobModule {

    private final Logger logger = LogManager.getLogger(AlarmJobModule.class);

    public AlarmJobModule() {
        logger.info("Install AlarmJobModule");
    }
}
