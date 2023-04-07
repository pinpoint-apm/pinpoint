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
import com.navercorp.pinpoint.login.basic.PinpointBasicLoginConfig;
import com.navercorp.pinpoint.metric.web.MetricWebApp;
import com.navercorp.pinpoint.uristat.web.UriStatWebConfig;
import com.navercorp.pinpoint.web.AuthorizationConfig;
import com.navercorp.pinpoint.web.PinpointWebModule;
import com.navercorp.pinpoint.web.WebApp;
import com.navercorp.pinpoint.web.WebStarter;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;
/**
 * @author minwoo.jung
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        TransactionAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
@Import({PinpointWebModule.class})
public class MetricAndWebApp {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(WebApp.class);

    public static void main(String[] args) {
        try {
            WebStarter starter = new WebStarter(MetricAndWebApp.class, PinpointBasicLoginConfig.class, AuthorizationConfig.class, MetricWebApp.class, UriStatWebConfig.class);
            starter.addProfiles("uri", "metric");
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[WebApp] could not launch app.", exception);
        }
    }


}
