/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.batch.config;

import com.navercorp.pinpoint.batch.alarm.AlarmMessageSender;
import com.navercorp.pinpoint.batch.alarm.DefaultAlarmMessageSender;
import com.navercorp.pinpoint.batch.common.BatchJobLauncher;
import com.navercorp.pinpoint.batch.common.BatchProperties;
import com.navercorp.pinpoint.batch.common.JobFailListener;
import com.navercorp.pinpoint.batch.common.JobFailMessageSender;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Optional;

import com.navercorp.pinpoint.batch.alarm.sender.MailSender;
import com.navercorp.pinpoint.batch.alarm.sender.SmsSender;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookSender;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.user.dao.mysql.MysqlUserGroupDao;
import com.navercorp.pinpoint.web.dao.mysql.MysqlUserDao;
import org.mybatis.spring.SqlSessionTemplate;

@Configuration
public class BatchInfrastructureConfig {

    @Bean
    public BatchProperties batchProperties() {
        return new BatchProperties();
    }

    @Bean
    public BatchJobLauncher batchJobLauncher(JobRegistry jobRegistry,
                                             JobLauncher jobLauncher,
                                             BatchProperties batchProperties) {
        return new BatchJobLauncher(jobRegistry, jobLauncher, batchProperties);
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public JobRepository jobRepository(
            @Qualifier("metaDataDataSource") DataSource dataSource,
            @Qualifier("metaDataTransactionManager") PlatformTransactionManager transactionManager) throws Exception {

        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor processor = new JobRegistryBeanPostProcessor();
        processor.setJobRegistry(jobRegistry);
        return processor;
    }

    @Bean
    public JobFailListener jobFailListener(Optional<JobFailMessageSender> jobFailMessageSender) {
        return new JobFailListener(jobFailMessageSender);
    }

    @Bean
    public AlarmMessageSender alarmMessageSender(
            MailSender mailSender,
            WebhookSender webhookSender,
            Optional<SmsSender> smsSender
    ) {
        return new DefaultAlarmMessageSender(mailSender, webhookSender, smsSender);
    }

    // ---- 기존 XML(applicationContext-batch-common.xml)에 있던 추가 빈들 ----
    // 다른 Job들(agentCount, cleanup 등)에서 사용될 수 있으므로 누락 방지 차원에서 등록

    @Bean
    public ConfigProperties configProperties() {
        return new ConfigProperties();
    }

    @Bean
    public MysqlUserGroupDao userGroupDao(
            @Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate
    ) {
        return new MysqlUserGroupDao(sqlSessionTemplate);
    }

    @Bean
    public MysqlUserDao userDao(
            @Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate
    ) {
        return new MysqlUserDao(sqlSessionTemplate);
    }
}
