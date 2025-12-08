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

package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseNamespaceConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseTemplateConfiguration;
import com.navercorp.pinpoint.common.server.CommonsHbaseConfiguration;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetadataDecoder;
import com.navercorp.pinpoint.common.server.hbase.config.HbaseClientConfiguration;
import com.navercorp.pinpoint.web.applicationmap.config.MapDaoConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({
        CommonsHbaseConfiguration.class,

        HbaseNamespaceConfiguration.class,
        DistributorConfiguration.class,

        HbaseClientConfiguration.class,
        HbaseTemplateConfiguration.class,
        MapDaoConfiguration.class
})
@ComponentScan(
        basePackages = {
                "com.navercorp.pinpoint.web.dao.hbase"
        }
)
@PropertySource(name = "WebHbaseModule", value = {
        "classpath:hbase-root.properties",
        "classpath:profiles/${pinpoint.profiles.active:release}/hbase.properties"
})
public class WebHbaseModule {
    private final Logger logger = LogManager.getLogger(getClass());

    public WebHbaseModule() {
        logger.info("Install {}", WebHbaseModule.class.getSimpleName());
    }


    @Bean
    public RowKeyDecoder<MetaDataRowKey> metadataDecoder() {
        return new MetadataDecoder();
    }
}
