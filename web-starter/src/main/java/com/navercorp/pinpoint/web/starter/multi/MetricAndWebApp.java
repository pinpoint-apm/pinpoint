/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.web.starter.multi;

import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import com.navercorp.pinpoint.metric.web.MetricWebApp;
import com.navercorp.pinpoint.web.WebApp;
import com.navercorp.pinpoint.web.WebMvcConfig;
import com.navercorp.pinpoint.web.WebStarter;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.util.Arrays;

/**
 * @author minwoo.jung
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, TransactionAutoConfiguration.class, BatchAutoConfiguration.class})
@ImportResource({ "classpath:applicationContext-web.xml", "classpath:servlet-context-web.xml"})
public class MetricAndWebApp {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(WebApp.class);


    public static void main(String[] args) {
        try {
            WebStarter starter = new WebStarter(MetricAndWebApp.class, WebMvcConfig.class, MetricWebApp.class);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[WebApp] could not launch app.", exception);
        }
    }


}
