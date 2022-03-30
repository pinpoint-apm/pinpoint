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

import com.navercorp.pinpoint.test.plugin.shared.AfterSharedClass;
import com.navercorp.pinpoint.test.plugin.shared.BeforeSharedClass;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Roy Kim
 */

public abstract class ElasticsearchITBase {

    public static EmbeddedElastic embeddedElastic;
    public static RestHighLevelClient restHighLevelClient;

    public static String ELASTICSEARCH_ADDRESS = "127.0.0.1:" + 9200;

    @BeforeSharedClass
    public static void sharedSetUp() throws Exception {
        embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("6.8.0")
                .withSetting(PopularProperties.HTTP_PORT, 9200)
                .withEsJavaOpts("-Xms128m -Xmx512m")
                .withStartTimeout(2, MINUTES)
                .build()
                .start();

    }

    @BeforeClass
    public static void setup() throws IOException {
        restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("127.0.0.1", 9200, "http")));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        if (restHighLevelClient == null) {
            restHighLevelClient.close();
        }
    }

    @AfterSharedClass
    public static void sharedTearDown() throws IOException {
        if (embeddedElastic != null)
            embeddedElastic.stop();
    }

}
