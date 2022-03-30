/*
 * Copyright 2020 NAVER Corp.
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

package com.pinpoint.test.plugin;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;

@RestController
public class ElasticSearchPluginController {

    public static EmbeddedElastic embeddedElastic;
    public static RestHighLevelClient restHighLevelClient;

    public static String ELASTICSEARCH_ADDRESS = "127.0.0.1:" + 9200;

    @PostConstruct
    private void start() throws Exception {
        embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("6.8.0")
                .withSetting(PopularProperties.HTTP_PORT, 9200)
                .withEsJavaOpts("-Xms128m -Xmx512m")
                .withStartTimeout(2, MINUTES)
                .build()
                .start();
    }

    @PreDestroy
    private void shutdown() {
        if (embeddedElastic != null)
            embeddedElastic.stop();
    }

    @GetMapping("/index")
    public Mono<String> index() throws Exception {
        restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("127.0.0.1", 9200, "http")));

        IndexRequest indexRequest = new IndexRequest(
                "post2");
        indexRequest.id("1");

        String jsonString = "{" +
                "\"user\":\"kimchy\"," +
                "\"postDate\":\"2013-01-30\"," +
                "\"message\":\"trying out Elasticsearch\"" +
                "}";

        indexRequest.source(jsonString, XContentType.JSON);
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

        TimeUnit.SECONDS.sleep(1);

        if (restHighLevelClient == null) {
            restHighLevelClient.close();
        }

        return Mono.just("OK");
    }
}
