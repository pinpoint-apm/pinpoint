/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.web.problem;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author intr3p1d
 */
@Configuration
public class ProblemSpringWebConfig {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Bean
    @Primary
    public ResponseEntityExceptionHandler pinpointExceptionHandling(
            @Value("${server.error.hostname:}") String hostnameAlias,
            ServerProperties serverProperties) {
        if (StringUtils.isEmpty(hostnameAlias)) {
            hostnameAlias = getHostName();
        }
        logger.info("server.error.hostname:{}", hostnameAlias);
        ErrorProperties error = serverProperties.getError();
        return new CustomExceptionHandler(hostnameAlias, error);
    }

    static private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
