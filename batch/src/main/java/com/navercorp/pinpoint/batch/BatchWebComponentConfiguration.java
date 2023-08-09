/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.batch;

import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageDeserializer;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageDeserializerV1;
import com.navercorp.pinpoint.common.server.util.DefaultTimeSlot;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.thrift.io.AgentEventHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.web.config.ScatterChartProperties;
import com.navercorp.pinpoint.web.config.UserConfigProperties;
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.SqlMetaDataDao;
import com.navercorp.pinpoint.web.dao.UserDao;
import com.navercorp.pinpoint.web.dao.UserGroupDao;
import com.navercorp.pinpoint.web.dao.hbase.HbaseSqlMetaDataDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.mapper.SqlMetaDataMapper;
import com.navercorp.pinpoint.web.service.AdminServiceImpl;
import com.navercorp.pinpoint.web.service.AgentEventService;
import com.navercorp.pinpoint.web.service.AgentEventServiceImpl;
import com.navercorp.pinpoint.web.service.AgentStatusService;
import com.navercorp.pinpoint.web.service.AgentStatusServiceImpl;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.service.AlarmServiceImpl;
import com.navercorp.pinpoint.web.service.ApplicationFactory;
import com.navercorp.pinpoint.web.service.DefaultApplicationFactory;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.service.UserGroupServiceImpl;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.service.UserServiceImpl;
import com.navercorp.pinpoint.web.util.UserInfoDecoder;
import com.navercorp.pinpoint.web.util.UserInfoEncoder;
import com.navercorp.pinpoint.web.vo.RangeFactory;
import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.web.mapper",
        "com.navercorp.pinpoint.common.server.bo",
        "com.navercorp.pinpoint.common.server.dao.hbase.mapper",
})
public class BatchWebComponentConfiguration {

    @Bean
    public ApplicationFactory applicationFactory(ServiceTypeRegistryService registry) {
        return new DefaultApplicationFactory(registry);
    }

    @Bean
    public TimeSlot timeSlot() {
        return new DefaultTimeSlot();
    }

    @Bean
    public RangeFactory rangeFactory(TimeSlot timeSlot) {
        return new RangeFactory(timeSlot);
    }

    @Bean
    public ScatterChartProperties scatterChartProperties() {
        return new ScatterChartProperties();
    }


    @Bean
    public SerializerFactory<HeaderTBaseSerializer> commandHeaderTBaseSerializerFactory() {
        return new CommandHeaderTBaseSerializerFactory();
    }


    @Bean
    public SqlMetaDataDao hbaseSqlMetaDataDao(HbaseOperations2 hbaseOperations2,
                                              TableNameProvider tableNameProvider,
                                              RowMapper<List<SqlMetaDataBo>> sqlMetaDataMapper,
                                              @Qualifier("metadataRowKeyDistributor2") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        return new HbaseSqlMetaDataDao(hbaseOperations2, tableNameProvider, sqlMetaDataMapper, rowKeyDistributorByHashPrefix);
    }


    @Bean
    public RowMapper<List<SqlMetaDataBo>> sqlMetaDataMapper(
            @Qualifier("metadataRowKeyDistributor2") RowKeyDistributorByHashPrefix rowKeyDistributorByHashPrefix) {
        return new SqlMetaDataMapper(rowKeyDistributorByHashPrefix);
    }


    // ----------------------------
    // Service
    // ----------------------------
    @Bean
    public AnnotationKeyRegistryService annotationKeyRegistryService(TraceMetadataLoaderService typeLoaderService) {
        return new DefaultAnnotationKeyRegistryService(typeLoaderService);
    }


    @Bean
    public AlarmService alarmService(AlarmDao alarmDao, ApplicationEventPublisher eventPublisher) {
        return new AlarmServiceImpl(alarmDao, eventPublisher);
    }

    @Bean
    public UserGroupService userGroupService(@Qualifier("mysqlUserGroupDao") UserGroupDao userGroupDao,
                                             Optional<UserInfoDecoder> userInfoDecoder,
                                             UserConfigProperties userConfigProperties,
                                             UserService userService) {
        return new UserGroupServiceImpl(userGroupDao, userInfoDecoder, userConfigProperties, userService);
    }


    @Bean
    public UserService userService(UserDao userDao,
                                   Optional<UserInfoDecoder> userInfoDecoder,
                                   Optional<UserInfoEncoder> userInfoEncoder) {
        return new UserServiceImpl(userDao, userInfoDecoder, userInfoEncoder);
    }

    @Bean
    public AgentStatusService agentStatusService(AgentStatDao<JvmGcBo> jvmGcDao,
                                                 AgentEventService agentEventService) {
        return new AgentStatusServiceImpl(jvmGcDao, agentEventService);
    }

    @Bean
    public AdminServiceImpl adminService(ApplicationIndexDao applicationIndexDao,
                                         AgentStatusService agentStatusService) {
        return new AdminServiceImpl(applicationIndexDao, agentStatusService);
    }


    @Bean
    public AgentEventMessageDeserializer agentEventMessageDeserializer() {
        List<DeserializerFactory<HeaderTBaseDeserializer>> deserializerFactoryList = List.of(
                CommandHeaderTBaseDeserializerFactory.getDefaultInstance(),
                new AgentEventHeaderTBaseDeserializerFactory()
        );
        return new AgentEventMessageDeserializer(deserializerFactoryList);
    }

    @Bean
    public AgentEventMessageDeserializerV1 agentEventMessageDeserializerV1() {
        return new AgentEventMessageDeserializerV1();
    }

    @Bean
    public AgentEventService agentEventService(AgentEventDao agentEventDao) {
        AgentEventMessageDeserializer agentEventMessageDeserializer = agentEventMessageDeserializer();
        AgentEventMessageDeserializerV1 agentEventMessageDeserializerV1 = agentEventMessageDeserializerV1();
        return new AgentEventServiceImpl(agentEventDao, agentEventMessageDeserializer, agentEventMessageDeserializerV1);
    }
}
