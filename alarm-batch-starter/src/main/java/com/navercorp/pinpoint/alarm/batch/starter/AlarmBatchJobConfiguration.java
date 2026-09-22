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

import com.navercorp.pinpoint.alarm.batch.config.AlarmJobConfiguration;
import com.navercorp.pinpoint.alarm.batch.config.AlarmJobSchedulerConfiguration;
import com.navercorp.pinpoint.alarm.batch.config.AlarmNotificationConfiguration;
import com.navercorp.pinpoint.alarm.batch.config.AlarmNotificationSchedulerConfiguration;
import com.navercorp.pinpoint.datasource.MainDataSourceConfiguration;
import com.navercorp.pinpoint.datasource.MainDataSourcePropertySource;
import com.navercorp.pinpoint.datasource.MetaDataSourceConfiguration;
import com.navercorp.pinpoint.mybatis.MyBatisConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Everything the alarm job needs that is not a place to read numbers from.
 *
 * <p>Separate from the data sources because it can be stood up on its own: the rules, the
 * schedules, the outbox and the senders all live in the alarm database, so this half boots
 * against a database and nothing else. What a rule may measure is added on top of it.
 *
 * <p>Autoconfiguration is declared by the application class, not here. Boot unions the
 * exclusions of every {@code @EnableAutoConfiguration} it finds, so carrying them on a class
 * meant to be imported would switch them off in whatever context imported it -- and the
 * modules of the other deployables ({@code BatchModule}, {@code WebModule},
 * {@code CollectorModule}) carry none for that reason. A context that boots this half alone
 * declares its own, the way this module's test does.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.alarm.evaluation",
        "com.navercorp.pinpoint.alarm.sender",
        "com.navercorp.pinpoint.alarm.service",
        "com.navercorp.pinpoint.alarm.validation"
})
@Import({
        AlarmBatchPropertySources.class,
        MainDataSourcePropertySource.class,
        MainDataSourceConfiguration.class,
        MetaDataSourceConfiguration.class,
        MyBatisConfiguration.class,

        AlarmBatchMetaDataConfiguration.class,
        AlarmBatchDaoConfiguration.class,

        AlarmJobConfiguration.class,
        AlarmJobSchedulerConfiguration.class,
        AlarmNotificationConfiguration.class,
        AlarmNotificationSchedulerConfiguration.class
})
public class AlarmBatchJobConfiguration {
}
