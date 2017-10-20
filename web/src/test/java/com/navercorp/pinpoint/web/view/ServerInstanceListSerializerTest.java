/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.view;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.ServerInstanceListTest;

import com.navercorp.pinpoint.web.util.ServiceTypeRegistryMockFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;

/**
 * @author emeroad
 */
public class ServerInstanceListSerializerTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ServiceTypeRegistryService mockServiceTypeRegistryService() {
        final short standAloneTypeCode = 1005;
        final String standAloneTypeName = "TEST_STAND_ALONE";

        ServiceTypeRegistryMockFactory mockFactory = new ServiceTypeRegistryMockFactory();
        mockFactory.addServiceTypeMock(standAloneTypeCode, standAloneTypeName);

        return mockFactory.createMockServiceTypeRegistryService();
    }

    @Test
    public void testSerialize() throws Exception {

        ObjectMapper mapper = createMapper();

        AgentInfo agentInfo = ServerInstanceListTest.createAgentInfo("agentId1", "testHost");

        Set<AgentInfo> agentInfoSet = new HashSet<>();
        agentInfoSet.add(agentInfo);

        ServerBuilder builder = new ServerBuilder();
        builder.addAgentInfo(agentInfoSet);

        ServerInstanceList serverInstanceList = builder.build();
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        String json = objectWriter.writeValueAsString(serverInstanceList);
        logger.debug(json);
    }


    private ObjectMapper createMapper() throws Exception {
        final Jackson2ObjectMapperFactoryBean factoryBean = new Jackson2ObjectMapperFactoryBean();

        final ServerInstanceSerializer serverInstanceSerializer = new ServerInstanceSerializer();

        final ServiceTypeRegistryService serviceTypeRegistryService = mockServiceTypeRegistryService();
        serverInstanceSerializer.setServiceTypeRegistryService(serviceTypeRegistryService);

        factoryBean.setHandlerInstantiator(new TestHandlerInstantiator());
        // TODO FIX spring managed object

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    public class TestHandlerInstantiator extends HandlerInstantiator {

        public TestHandlerInstantiator() {
        }

        public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
            if (annotated.getName().equals("com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance")) {
                final ServiceTypeRegistryService serviceTypeRegistryService = mockServiceTypeRegistryService();

                final ServerInstanceSerializer serverInstanceSerializer = new ServerInstanceSerializer();
                serverInstanceSerializer.setServiceTypeRegistryService(serviceTypeRegistryService);

                final AgentLifeCycleStateSerializer agentLifeCycleStateSerializer = new AgentLifeCycleStateSerializer();
                serverInstanceSerializer.setAgentLifeCycleStateSerializer(agentLifeCycleStateSerializer);
                return serverInstanceSerializer;
            }
            return null;
        }

        @Override
        public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
            return null;
        }

        @Override
        public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
            return null;
        }

        @Override
        public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
            return null;
        }

        @Override
        public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
            return null;
        }
    }


}
