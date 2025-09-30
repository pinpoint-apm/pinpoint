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

package com.navercorp.pinpoint.collector;


import com.navercorp.pinpoint.collector.applicationmap.config.ApplicationMapModule;
import com.navercorp.pinpoint.collector.config.ClusterModule;
import com.navercorp.pinpoint.collector.config.CollectorCommonConfiguration;
import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import com.navercorp.pinpoint.collector.event.config.CollectorEventConfiguration;
import com.navercorp.pinpoint.collector.grpc.CollectorGrpcConfiguration;
import com.navercorp.pinpoint.collector.grpc.ssl.GrpcSslModule;
import com.navercorp.pinpoint.collector.heatmap.HeatmapCollectorModule;
import com.navercorp.pinpoint.collector.manage.CollectorAdminConfiguration;
import com.navercorp.pinpoint.collector.uid.CollectorUidConfiguration;
import com.navercorp.pinpoint.common.server.CommonsServerConfiguration;
import com.navercorp.pinpoint.common.server.config.TypeLoaderConfiguration;
import com.navercorp.pinpoint.common.server.uid.ObjectNameVersion;
import com.navercorp.pinpoint.realtime.collector.RealtimeCollectorModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        CollectorAppPropertySources.class,
        CommonsServerConfiguration.class,
        CollectorAdminConfiguration.class,
        CollectorMvcConfig.class,

        CollectorCommonConfiguration.class,

        TypeLoaderConfiguration.class,

        CollectorConfiguration.class,
        CollectorHbaseModule.class,

        CollectorGrpcConfiguration.class,

        ClusterModule.class,

        GrpcSslModule.class,

        RealtimeCollectorModule.class,

        ApplicationMapModule.class,

        CollectorUidConfiguration.class,
        HeatmapCollectorModule.class,

        CollectorEventConfiguration.class
})
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.collector.handler",
        "com.navercorp.pinpoint.collector.manage",
        "com.navercorp.pinpoint.collector.mapper",
        "com.navercorp.pinpoint.collector.util",
        "com.navercorp.pinpoint.collector.service",
        "com.navercorp.pinpoint.collector.controller",
})
public class PinpointCollectorModule {


    @Bean
    public ObjectNameVersion serverNameVersion(@Value(ObjectNameVersion.VALUE_KEY) String version) {
        return ObjectNameVersion.getVersion(version);
    }

}
