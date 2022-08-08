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

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.navercorp.pinpoint.web.applicationmap.ServerGroupListTest;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;

import java.util.Set;

/**
 * @author emeroad
 */
public class ServerGroupListSerializerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testSerialize() throws Exception {

        ObjectMapper mapper = createMapper();

        AgentAndStatus agentInfo = ServerGroupListTest.createAgentInfo("agentId1", "testHost");
        Set<AgentAndStatus> agentInfoSet = Set.of(agentInfo);

        ServerBuilder builder = new ServerBuilder();
        builder.addAgentInfo(agentInfoSet);

        ServerGroupList serverGroupList = builder.build();
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        String json = objectWriter.writeValueAsString(serverGroupList);
        logger.debug(json);
    }


    private ObjectMapper createMapper() {
        final Jackson2ObjectMapperFactoryBean factoryBean = new Jackson2ObjectMapperFactoryBean();

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
                return new ServerInstanceSerializer();
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
