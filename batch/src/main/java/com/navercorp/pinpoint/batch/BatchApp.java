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

package com.navercorp.pinpoint.batch;

import com.navercorp.pinpoint.batch.alarm.AlarmSenderConfiguration;
import com.navercorp.pinpoint.common.server.config.RestTemplateConfiguration;
import com.navercorp.pinpoint.common.server.config.TypeLoaderConfiguration;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.web.UserModule;
import com.navercorp.pinpoint.web.WebHbaseModule;
import com.navercorp.pinpoint.web.webhook.WebhookModule;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * @author minwoo.jung
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        TransactionAutoConfiguration.class,
        BatchAutoConfiguration.class
})
@ImportResource({
        "classpath:applicationContext-batch-schedule.xml"
})
@Import({
        TypeLoaderConfiguration.class,
        BatchAppPropertySources.class,
        WebhookModule.class,
        WebHbaseModule.class,
        RestTemplateConfiguration.class,
        UserModule.class,
        UriStatAlarmConfiguration.class,
        AlarmSenderConfiguration.class
})
public class BatchApp {

    private static final ServerBootLogger logger = ServerBootLogger.getLogger(BatchApp.class);


    public static void main(String[] args) {
        try {
            BatchStarter starter = new BatchStarter(BatchApp.class);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[WebApp] could not launch app.", exception);
        }
    }
}
