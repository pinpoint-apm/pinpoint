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
import com.navercorp.pinpoint.web.PinpointBasicLoginConfig;
import com.navercorp.pinpoint.web.WebApp;
import com.navercorp.pinpoint.web.WebAppPropertySources;
import com.navercorp.pinpoint.web.WebMvcConfig;
import com.navercorp.pinpoint.web.WebServerConfig;
import com.navercorp.pinpoint.web.WebStarter;
import com.navercorp.pinpoint.web.cache.CacheConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
/**
 * @author minwoo.jung
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, TransactionAutoConfiguration.class,
        SecurityAutoConfiguration.class})
@ImportResource({"classpath:applicationContext-web.xml", "classpath:servlet-context-web.xml"})
@Import({WebAppPropertySources.class, WebServerConfig.class, WebMvcConfig.class, CacheConfiguration.class})
public class MetricAndWebApp {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(WebApp.class);

    public static void main(String[] args) {
        try {
            WebStarter starter = new WebStarter(MetricAndWebApp.class, PinpointBasicLoginConfig.class, MetricWebApp.class);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[WebApp] could not launch app.", exception);
        }
    }


}
