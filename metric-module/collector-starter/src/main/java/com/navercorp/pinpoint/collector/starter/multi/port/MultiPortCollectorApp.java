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

package com.navercorp.pinpoint.collector.starter.multi.port;

import com.navercorp.pinpoint.collector.CollectorStarter;
import com.navercorp.pinpoint.common.server.util.ServerBootLogger;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Deprecated
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ImportResource({"classpath:applicationContext-collector.xml", "classpath:servlet-context-collector.xml", "classpath:pinot-collector/applicationContext-collector-pinot.xml", "classpath:pinot-collector/servlet-context-collector-pinot.xml"})
@Import(EmbeddedTomcatConfiguration.class)
public class MultiPortCollectorApp {
    private static final ServerBootLogger logger = ServerBootLogger.getLogger(com.navercorp.pinpoint.collector.starter.CollectorApp.class);


    public static void main(String[] args) {
        try {
            CollectorStarter starter = new CollectorStarter(MultiPortCollectorApp.class);
            starter.start(args);
        } catch (Exception exception) {
            logger.error("[MultiPortCollectorApp] could not launch app.", exception);
        }
    }

}
