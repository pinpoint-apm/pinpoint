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

package com.navercorp.pinpoint.otlp.trace.collector;

import com.navercorp.pinpoint.collector.config.SchedulerConfiguration;
import com.navercorp.pinpoint.collector.event.config.CollectorEventConfiguration;
import com.navercorp.pinpoint.collector.scatter.ScatterCollectorConfiguration;
import com.navercorp.pinpoint.collector.util.DurabilityApplier;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseNamespaceConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbasePutWriterConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseTemplateConfiguration;
import com.navercorp.pinpoint.common.server.CommonsHbaseConfiguration;
import com.navercorp.pinpoint.common.server.CommonsServerConfiguration;
import com.navercorp.pinpoint.common.server.config.CommonCacheManagerConfiguration;
import com.navercorp.pinpoint.common.server.hbase.config.HbaseClientConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Import({
        CommonsServerConfiguration.class,
        CommonsHbaseConfiguration.class,
        HbaseNamespaceConfiguration.class,
        DistributorConfiguration.class,

        HbaseClientConfiguration.class,
        HbaseTemplateConfiguration.class,
        HbasePutWriterConfiguration.class,

        // collector
        CommonCacheManagerConfiguration.class,
        SchedulerConfiguration.class,
        CollectorEventConfiguration.class,

        ScatterCollectorConfiguration.class
})
@ComponentScan({
        "com.navercorp.pinpoint.collector.dao.hbase",
})
@PropertySource(name = "OtlpTraceCollectorHbaseModule", value = {
        "classpath:hbase-root.properties",
        "classpath:profiles/${pinpoint.profiles.active:local}/hbase.properties"
})
public class OtlpTraceCollectorHbaseModule {
    private final Logger logger = LogManager.getLogger(OtlpTraceCollectorHbaseModule.class);

    // TraceDao
    @Bean
    public DurabilityApplier spanPutWriterDurabilityApplier(@Value("${collector.span.durability:USE_DEFAULT}") String spanDurability) {
        logger.info("Span(Trace Put) durability:{}", spanDurability);
        return new DurabilityApplier(spanDurability);
    }
}
