/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.batch.starter;

import com.navercorp.pinpoint.applicationmap.config.MapDaoConfiguration;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseNamespaceConfiguration;
import com.navercorp.pinpoint.common.hbase.config.HbaseTemplateConfiguration;
import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.CommonsHbaseConfiguration;
import com.navercorp.pinpoint.common.server.CommonsServerConfiguration;
import com.navercorp.pinpoint.common.server.config.CommonCacheManagerConfiguration;
import com.navercorp.pinpoint.common.server.bo.ApplicationFactory;
import com.navercorp.pinpoint.common.server.config.TypeLoaderConfiguration;
import com.navercorp.pinpoint.common.server.hbase.config.HbaseClientConfiguration;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.service.component.DefaultApplicationFactory;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.service.ServiceRegistryServiceImpl;
import com.navercorp.pinpoint.service.config.ServiceMysqlConfiguration;
import com.navercorp.pinpoint.service.dao.ServiceRegistryDao;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.RandomServiceUidGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * The hbase side of what a rule can measure: the response and out-call histograms the server
 * map is drawn from, and the events an agent reports about itself.
 *
 * <p>These are the same stores and the same dao beans the web and the batch read. They are
 * assembled here rather than imported from either, because neither assembles them alone --
 * the batch reaches them through a module named for the web -- and a process that only
 * evaluates alarms should not carry a web module to do it.
 */
@Configuration
@Import({
        CommonsServerConfiguration.class,
        TypeLoaderConfiguration.class,
        // The hbase client registers two cache managers of its own and turns caching on, so
        // a context that installs it needs the primary one to resolve against.
        CommonCacheManagerConfiguration.class,

        CommonsHbaseConfiguration.class,
        HbaseNamespaceConfiguration.class,
        DistributorConfiguration.class,
        HbaseClientConfiguration.class,
        HbaseTemplateConfiguration.class,
        MapDaoConfiguration.class,

        ServiceMysqlConfiguration.class
})
// The agent event dao the deadlock metric reads, and the row mappers it needs: declared by
// annotation rather than as beans, so they have to be scanned for. The agent id reader in the
// same package is not annotated and is declared below instead -- the web and the batch scan
// this package too, and a bean only this process wants has no business in theirs.
@ComponentScan("com.navercorp.pinpoint.common.server.dao.hbase")
@PropertySource(name = "AlarmBatchHbaseConfiguration", value = {
        "classpath:hbase-root.properties",
        "classpath:profiles/${pinpoint.profiles.active:release}/hbase.properties"
})
public class AlarmBatchHbaseConfiguration {

    /**
     * Resolves the service a row key belongs to. The map row keys carry a service uid and the
     * application objects the mappers build name the service, so the registry is on the read
     * path even though an alarm never registers a service -- the generator is here only
     * because the implementation insists on one.
     */
    @Bean
    public IdGenerator<ServiceUid> serviceUidGenerator() {
        return new RandomServiceUidGenerator();
    }

    @Bean
    public ServiceRegistryService serviceRegistryService(ServiceRegistryDao serviceRegistryDao,
                                                        IdGenerator<ServiceUid> serviceUidGenerator) {
        return new ServiceRegistryServiceImpl(serviceRegistryDao, serviceUidGenerator);
    }

    @Bean
    public ServiceModelResolver serviceModelResolver(ServiceRegistryService serviceRegistryService) {
        return new ServiceModelResolver(serviceRegistryService);
    }

    @Bean
    public ApplicationFactory applicationFactory(ServiceTypeRegistryService registry,
                                                 ServiceModelResolver serviceModelResolver) {
        return new DefaultApplicationFactory(registry, serviceModelResolver);
    }
}
