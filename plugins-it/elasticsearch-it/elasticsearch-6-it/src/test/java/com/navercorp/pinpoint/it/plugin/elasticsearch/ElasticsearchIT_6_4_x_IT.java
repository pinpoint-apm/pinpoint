package com.navercorp.pinpoint.it.plugin.elasticsearch;
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
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.elasticsearch.client:elasticsearch-rest-high-level-client:[6.4.0,6.9.9)"})
@SharedDependency({PluginITConstants.VERSION, TestcontainersOption.ELASTICSEARCH})
@SharedTestLifeCycleClass(ESServer.class)
public class ElasticsearchIT_6_4_x_IT extends ElasticsearchITBase {

    private static RestHighLevelClient restHighLevelClient;

    @BeforeEach
    public void setup() {
        ES_PORT = Integer.parseInt(System.getProperty("PORT"));

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

        testIndexV64UP(verifier);
    }

    @SuppressWarnings("deprecation")
    private void testIndexV64UP(PluginTestVerifier verifier) throws IOException {

        IndexRequest indexRequest = new IndexRequest("postv6", "doc", "3");

        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";
        indexRequest.source(jsonString, XContentType.JSON);

        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

        Method index;
        try {
            index = restHighLevelClient.getClass().getDeclaredMethod("index", IndexRequest.class, RequestOptions.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }

        verifier.verifyTrace(event("ELASTICSEARCH_HIGHLEVEL_CLIENT", index, null, getEsAddress(), "ElasticSearch"
                , new ExpectedAnnotation("es.dsl", indexRequest.toString())
        ));
    }

}
