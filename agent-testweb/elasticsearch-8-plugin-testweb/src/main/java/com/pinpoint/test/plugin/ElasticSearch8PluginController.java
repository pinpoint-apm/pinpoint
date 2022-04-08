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

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static java.util.concurrent.TimeUnit.MINUTES;

@RestController
public class ElasticSearch8PluginController {

    public static EmbeddedElastic embeddedElastic;

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
        // Create the low-level client
        RestClient restClient = RestClient.builder(
                new HttpHost("127.0.0.1", 9200)).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient client = new ElasticsearchClient(transport);

        if (client.exists(b -> b.index("products").id("foo")).value()) {
            System.out.println("product exists");
        }

        // Asynchronous non-blocking client
        ElasticsearchAsyncClient asyncClient =
                new ElasticsearchAsyncClient(transport);

        asyncClient
                .exists(b -> b.index("products").id("foo"))
                .thenAccept(response -> {
                    if (response.value()) {
                        System.out.println("product exists");
                    }
                });

        return Mono.just("OK");
    }
}
