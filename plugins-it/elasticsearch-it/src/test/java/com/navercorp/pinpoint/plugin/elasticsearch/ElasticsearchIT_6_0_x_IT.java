package com.navercorp.pinpoint.plugin.elasticsearch;
/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

@PinpointAgent(AgentPath.PATH)
@Dependency({"org.elasticsearch.client:elasticsearch-rest-high-level-client:[6.0.0,6.3.0)",
        TestcontainersOption.ELASTICSEARCH})
@JvmVersion(8)
@SharedTestLifeCycleClass(ESServer.class)
public class ElasticsearchIT_6_0_x_IT extends ElasticsearchITBase {

    private RestHighLevelClient restHighLevelClient;

    @BeforeEach
    public void setup() {
        restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(getEsHost(), getEsPort(), "http")));
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (restHighLevelClient != null) {
            restHighLevelClient.close();
        }
    }

    @Test
    public void testCRUD() throws Exception {

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        testIndexV60UP(verifier);
    }

    private void testIndexV60UP(PluginTestVerifier verifier) throws IOException {

        IndexRequest indexRequest = new IndexRequest("postv6", "doc", "3");

        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        indexRequest.source(jsonString, XContentType.JSON);

        Class<?> clazz;
        try {
            clazz = Class.forName("org.elasticsearch.client.RestHighLevelClient");
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        Method method;

        try {
            method = clazz.getMethod("index", IndexRequest.class, Class.forName("[Lorg.apache.http.Header;"));
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }

        try {
            method.invoke(restHighLevelClient, indexRequest, new Header[]{});
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(e);
        }

        Method index;
        try {
            index = restHighLevelClient.getClass().getDeclaredMethod("index", IndexRequest.class, Class.forName("[Lorg.apache.http.Header;"));
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }

        verifier.verifyTrace(event(ElasticsearchConstants.ELASTICSEARCH_EXECUTOR.getName(), index, null, getEsAddress(), "ElasticSearch"
                , new ExpectedAnnotation(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY.getName(), indexRequest.toString())
        ));
    }

}
