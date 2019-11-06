/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.web.batch;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.ImportResource;

import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 *
 */
@Configuration
@Conditional(BatchConfiguration.Condition.class)
@ImportResource("classpath:/batch/applicationContext-batch-schedule.xml")
public class BatchConfiguration implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("#{batchProps['batch.enable'] ?: false}")
    private boolean enableBatch;

    @Value("#{T(com.navercorp.pinpoint.common.util.StringUtils).tokenizeToStringList((batchProps['batch.flink.server'] ?: ''), ',')}")
    private List<String> flinkServerList = Collections.emptyList();

    @Value("#{batchProps['batch.server.ip'] ?: null}")
    private String batchServerIp;

    @Value("#{batchProps['alarm.mail.server.url']}")
    private String emailServerUrl;

    @Value("#{batchProps['alarm.mail.sender.address']}")
    private String senderEmailAddress;

    @Value("#{batchProps['pinpoint.url']}")
    private String pinpointUrl;

    @Value("#{batchProps['batch.server.env']}")
    private String batchEnv;


    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("BatchConfiguration:{}", this.toString());
    }

    public String getPinpointUrl() {
        return pinpointUrl;
    }

    public String getBatchServerIp() {
        return batchServerIp;
    }

    public List<String> getFlinkServerList() {
        return flinkServerList;
    }

    public String getEmailServerUrl() {
        return emailServerUrl;
    }

    public String getBatchEnv() {
        return batchEnv;
    }

    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }

    static class Condition implements ConfigurationCondition {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        public Condition() {
        }
        @Override
        public ConfigurationPhase getConfigurationPhase() {
            return ConfigurationPhase.PARSE_CONFIGURATION;
        }
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Properties batchProps = context.getBeanFactory().getBean("batchProps", Properties.class);
            final String enable = batchProps.getProperty("batch.enable", "false").trim();
            logger.info("batch.enable:{}", enable);
            return Boolean.valueOf(enable);
        }

    }

    @Override
    public String toString() {
        return "BatchConfiguration{" +
                "enableBatch=" + enableBatch +
                ", flinkServerList=" + flinkServerList +
                ", batchServerIp='" + batchServerIp + '\'' +
                '}';
    }
}
