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

package com.navercorp.pinpoint.batch;

import com.navercorp.pinpoint.batch.alarm.AlarmSenderConfiguration;
import com.navercorp.pinpoint.batch.common.BatchJobLauncher;
import com.navercorp.pinpoint.batch.common.StartupJobLauncher;
import com.navercorp.pinpoint.batch.configuration.AlarmJobModule;
import com.navercorp.pinpoint.batch.configuration.CleanupInactiveApplicationsJobConfig;
import com.navercorp.pinpoint.common.server.config.CommonCacheManagerConfiguration;
import com.navercorp.pinpoint.common.server.config.RestTemplateConfiguration;
import com.navercorp.pinpoint.common.server.util.DefaultTimeSlot;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.datasource.MainDataSourceConfiguration;
import com.navercorp.pinpoint.datasource.MetaDataSourceConfiguration;
import com.navercorp.pinpoint.web.UserModule;
import com.navercorp.pinpoint.web.WebHbaseModule;
import com.navercorp.pinpoint.web.WebServiceConfig;
import com.navercorp.pinpoint.web.component.config.ComponentConfiguration;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkConfiguration;
import com.navercorp.pinpoint.web.webhook.WebhookModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import java.util.List;

@ImportResource({
        "classpath:applicationContext-batch-schedule.xml",

        "classpath:applicationContext-batch-common.xml",
        "classpath:applicationContext-batch-dao-config.xml",
        "classpath:applicationContext-batch-web-component.xml",

        "classpath:job/applicationContext-agentCountJob.xml",
        "classpath:job/applicationContext-flinkCheckJob.xml",
        "classpath:job/applicationContext-cleanupInactiveAgentsJob.xml"
})
@Import({
        TransactionAutoConfiguration.class,
        BatchAppPropertySources.class,

        ComponentConfiguration.class,
        HyperLinkConfiguration.class,

        MainDataSourceConfiguration.class,
        MetaDataSourceConfiguration.class,

        AlarmJobModule.class,

        WebServiceConfig.CommonConfig.class,
        WebhookModule.class,
        WebHbaseModule.class,
        RestTemplateConfiguration.class,
        UserModule.class,
        UriStatAlarmConfiguration.class,
        AlarmSenderConfiguration.class,
        CommonCacheManagerConfiguration.class,
        CleanupInactiveApplicationsJobConfig.class,
})
public class BatchModule {

    @Bean
    public TimeSlot timeSlot() {
        return new DefaultTimeSlot();
    }

    @Bean
    StartupJobLauncher startupJobLauncher(
            BatchJobLauncher launcher,
            @Value("${batch.startup.jobs:#{''}}") List<String> jobs
    ) {
        return new StartupJobLauncher(launcher, jobs);
    }
}