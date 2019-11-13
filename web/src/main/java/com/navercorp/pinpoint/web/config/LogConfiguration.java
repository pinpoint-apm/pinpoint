/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
public class LogConfiguration {
    private final Logger logger = LoggerFactory.getLogger(LogConfiguration.class);

    @Value("${log.enable:false}")
    private boolean logLinkEnable;

    @Value("${log.button.name:}")
    private String logButtonName;

    @Value("${log.page.url:}")
    private String logPageUrl;

    @Value("${log.button.disable.message:}")
    private String disableButtonMessage;

    public boolean isLogLinkEnable() {
        return logLinkEnable;
    }

    public String getLogButtonName() {
        return logButtonName;
    }

    public String getLogPageUrl() {
        return logPageUrl;
    }

    public String getDisableButtonMessage() {
        return disableButtonMessage;
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor annotationVisitor = new AnnotationVisitor(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LogConfiguration{");
        sb.append("logLinkEnable=").append(logLinkEnable);
        sb.append(", logButtonName='").append(logButtonName).append('\'');
        sb.append(", logPageUrl='").append(logPageUrl).append('\'');
        sb.append(", disableButtonMessage='").append(disableButtonMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
