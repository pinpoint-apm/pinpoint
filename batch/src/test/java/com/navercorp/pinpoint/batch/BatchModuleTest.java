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

package com.navercorp.pinpoint.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.batch.config.CleanupAgentAndApplicationJobConfig;
import com.navercorp.pinpoint.common.hbase.HbaseVersionCheckBean;
import com.navercorp.pinpoint.pinot.datasource.PinotDataSource;
import com.navercorp.pinpoint.service.service.CachingServiceModelResolver;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {BatchModule.class, BatchModuleTest.TestDependency.class})
@ExtendWith(SpringExtension.class)
public class BatchModuleTest {

    @Autowired
    ServiceModelResolver serviceModelResolver;

    @Autowired
    JobRegistry jobRegistry;

    @Test
    public void serviceModelResolverIsCached() {
        assertThat(serviceModelResolver).isInstanceOf(CachingServiceModelResolver.class);
    }

    // BatchJobLauncher looks these up by name at cron time, so a mismatch would not fail at startup
    @Test
    public void launchedJobsAreRegistered() throws Exception {
        List<String> jobNames = List.of("alarmJob", "uriAlarmJob", "agentCountJob", CleanupAgentAndApplicationJobConfig.JOB_NAME);
        for (String jobName : jobNames) {
            assertThat(jobRegistry.getJob(jobName).getName()).isEqualTo(jobName);
        }
    }

    @Configuration
    static class TestDependency {

        // JDBC: real pools require a reachable database
        @Bean
        public DataSource dataSource() {
            return Mockito.mock(DataSource.class);
        }

        @Bean
        public DataSource metaDataDataSource() {
            return Mockito.mock(DataSource.class);
        }

        @Bean
        public PinotDataSource pinotDataSource() {
            return Mockito.mock(PinotDataSource.class);
        }

        // Spring Batch: the default repository reads JDBC metadata on startup
        @Bean
        public JobRepository jobRepository() {
            return Mockito.mock(JobRepository.class);
        }

        // HBase: connections block on ZooKeeper, version check queries the cluster
        @Bean
        public Connection hbaseConnection() {
            return Mockito.mock(Connection.class);
        }

        @Bean
        public AsyncConnection hbaseAsyncConnection() {
            return Mockito.mock(AsyncConnection.class);
        }

        @Bean
        public Connection mapHbaseConnection() {
            return Mockito.mock(Connection.class);
        }

        @Bean
        public AsyncConnection mapHbaseAsyncConnection() {
            return Mockito.mock(AsyncConnection.class);
        }

        @Bean
        public HbaseVersionCheckBean hbaseVersionCheck() {
            return Mockito.mock(HbaseVersionCheckBean.class);
        }

        // Spring Boot auto-configuration is not active in this plain test context
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public RestTemplateBuilderConfigurer restTemplateBuilderConfigurer() {
            return new RestTemplateBuilderConfigurer();
        }

        @Bean
        public static ConversionService conversionService() {
            return ApplicationConversionService.getSharedInstance();
        }
    }
}
