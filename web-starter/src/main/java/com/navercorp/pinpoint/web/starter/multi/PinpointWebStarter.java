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
import com.navercorp.pinpoint.datasource.MainDataSourcePropertySource;
import com.navercorp.pinpoint.exceptiontrace.web.ExceptionTraceWebConfig;
import com.navercorp.pinpoint.inspector.web.InspectorWebConfig;
import com.navercorp.pinpoint.log.web.LogWebModule;
import com.navercorp.pinpoint.login.basic.PinpointBasicLoginConfig;
import com.navercorp.pinpoint.metric.web.MetricWebApp;
import com.navercorp.pinpoint.otlp.web.OtlpMetricWebConfig;
import com.navercorp.pinpoint.redis.RedisPropertySources;
import com.navercorp.pinpoint.uristat.web.UriStatWebConfig;
import com.navercorp.pinpoint.web.AuthorizationConfig;
import com.navercorp.pinpoint.web.PinpointWebModule;
import com.navercorp.pinpoint.web.WebApp;
import com.navercorp.pinpoint.web.WebStarter;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.annotation.Import;
/**
 * @author minwoo.jung
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        TransactionAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        SpringDataWebAutoConfiguration.class,
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class
})
@Import({
        PinpointWebModule.class,
        MainDataSourcePropertySource.class,
        RedisPropertySources.class,
})
public class PinpointWebStarter {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(WebApp.class);

    public static void main(String[] args) {
        try {
            WebStarter starter = new WebStarter(
                    PinpointWebStarter.class,
                    PinpointBasicLoginConfig.class,
                    AuthorizationConfig.class,
                    MetricWebApp.class,
                    UriStatWebConfig.class,
                    InspectorWebConfig.class,
                    LogWebModule.class,
                    ExceptionTraceWebConfig.class,
                    OtlpMetricWebConfig.class
            );
            starter.addProfiles("uri", "metric");
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[WebApp] could not launch app.", exception);
        }
    }


}
